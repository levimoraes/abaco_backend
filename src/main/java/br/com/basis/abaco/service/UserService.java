package br.com.basis.abaco.service;

import br.com.basis.abaco.config.Constants;
import br.com.basis.abaco.domain.Authority;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.repository.AuthorityRepository;
import br.com.basis.abaco.repository.UserRepository;
import br.com.basis.abaco.repository.search.UserSearchRepository;
import br.com.basis.abaco.security.AuthoritiesConstants;
import br.com.basis.abaco.security.SecurityUtils;
import br.com.basis.abaco.service.dto.UserDTO;
import br.com.basis.abaco.service.util.RandomUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final SocialService socialService;

    private final UserSearchRepository userSearchRepository;

    private final AuthorityRepository authorityRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SocialService socialService,
                       UserSearchRepository userSearchRepository, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.socialService = socialService;
        this.userSearchRepository = userSearchRepository;
        this.authorityRepository = authorityRepository;
    }

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key).map(user -> {
            // activate given user for the registration key.
            user.setActivated(true);
            user.setActivationKey(null);
            userSearchRepository.save(user);
            log.debug("Activated user: {}", user);
            return user;
        });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);

        return userRepository.findOneByResetKey(key).filter(user -> {
            ZonedDateTime oneDayAgo = ZonedDateTime.now().minusHours(24);
            return user.getResetDate().isAfter(oneDayAgo);
        }).map(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetKey(null);
            user.setResetDate(null);
            return user;
        });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmail(mail).filter(User::getActivated).map(user -> {
            user.setResetKey(RandomUtil.generateResetKey());
            user.setResetDate(ZonedDateTime.now());
            return user;
        });
    }

    public User createUser(String login, String password, String firstName, String lastName, String email,
                           String imageUrl, String langKey) {

        User newUser = new User();
        Authority authority = authorityRepository.findOne(AuthoritiesConstants.USER);
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setImageUrl(imageUrl);
        newUser.setLangKey(langKey);
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        userSearchRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser); return newUser;
    }

    public User createUser(UserDTO userDTO) {
        User user = new User();
        setBaseUserProperties(userDTO, user);
        if (userDTO.getLangKey() == null) {
            // default language
            user.setLangKey("pt-br");
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = new HashSet<>();
            Optional.ofNullable(userDTO.getAuthorities()).orElse(Collections.emptySet())
                .forEach(authority -> authorities.add(authorityRepository.findOne(authority)));
            user.setAuthorities(authorities);
        }
        setUserProperties(user);
        userRepository.save(user);
        userSearchRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    private void setBaseUserProperties(UserDTO userDTO, User user) {
        user.setLogin(userDTO.getLogin());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setImageUrl(userDTO.getImageUrl());
    }

    private void setUserProperties(User user) {
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(ZonedDateTime.now());
        user.setActivated(true);
    }

    /**
     * Copies (shallow) an User and then: 1 - Set language key if not present 2 -
     * Set a generated password 3 - Set a generated resetKey
     *
     * @param user
     * @return
     */
    public User prepareUserToBeSaved(User user) {
        User userCopy = shallowCopyUser(user);
        String encryptedPassword = passwordEncoder.encode(userCopy.getPassword());
        userCopy.setPassword(encryptedPassword);
        userCopy.setResetKey(RandomUtil.generateResetKey());
        userCopy.setResetDate(ZonedDateTime.now());
        return userCopy;
    }

    public User generateUpdatableUser(User userToBeUpdated) {
        User userPreUpdate = userRepository.findOne(userToBeUpdated.getId());
        User updatableUser = shallowCopyUser(userToBeUpdated);
        if (updatableUser.getPassword() == null) {
            updatableUser.setPassword(userPreUpdate.getPassword());
        }
        if (updatableUser.getLangKey() == null) {
            updatableUser.setLangKey(userPreUpdate.getLangKey());
        }

        return updatableUser;
    }

    private User shallowCopyUser(User user) {
        User copy = new User();
        copy.setId(user.getId());
        copy.setLogin(user.getLogin());
        copy.setPassword(user.getPassword());
        copy.setFirstName(user.getFirstName());
        copy.setLastName(user.getLastName());
        copy.setEmail(user.getEmail());
        copy.setActivated(user.getActivated());
        copy.setLangKey(user.getLangKey());
        copy.setImageUrl(user.getImageUrl());
        copy.setActivationKey(user.getActivationKey());
        copy.setResetKey(user.getResetKey());
        copy.setResetDate(user.getResetDate());
        copy.setAuthorities(user.getAuthorities());
        copy.setTipoEquipes(user.getTipoEquipes());
        copy.setOrganizacoes(user.getOrganizacoes());
        return copy;
    }

    /**
     * Update basic information (first name, last name, email, language) for the
     * current user.
     */
    public void updateUser(String firstName, String lastName, String email, String langKey) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent(user -> {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setLangKey(langKey);
            userSearchRepository.save(user);
            log.debug("Changed Information for User: {}", user);
        });
    }

    /**
     * Update all information for a specific user, and return the modified user.
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
        return Optional.of(userRepository.findOne(userDTO.getId())).map(user -> {
            user.setLogin(userDTO.getLogin());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setImageUrl(userDTO.getImageUrl());
            user.setActivated(userDTO.isActivated());
            user.setLangKey(userDTO.getLangKey());
            Set<Authority> managedAuthorities = user.getAuthorities();
            managedAuthorities.clear();
            userDTO.getAuthorities().stream().map(authorityRepository::findOne).forEach(managedAuthorities::add);
            log.debug("Changed Information for User: {}", user);
            return user;
        }).map(UserDTO::new);
    }

    public void deleteUser(Long id) {
        userRepository.findOneById(id).ifPresent(user -> {
            socialService.deleteUserSocialConnection(user.getLogin());
            userRepository.delete(user);
            userSearchRepository.delete(user);
            log.debug("Deleted User: {}", user);
        });
    }

    public void changePassword(String password) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent(user -> {
            String encryptedPassword = passwordEncoder.encode(password);
            user.setPassword(encryptedPassword);
            log.debug("Changed password for User: {}", user);
        });
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities(Long id) {
        return userRepository.findOneWithAuthoritiesById(id);
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities() {
        return userRepository.findOneWithAuthoritiesByLogin(SecurityUtils.getCurrentUserLogin()).orElse(null);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     * </p>
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        ZonedDateTime now = ZonedDateTime.now();
        List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minusDays(3));
        for (User user : users) {
            log.debug("Deleting not activated user {}", user.getLogin());
            userRepository.delete(user);
            userSearchRepository.delete(user);
        }
    }
}
