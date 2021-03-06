package br.com.basis.abaco.service.mapper;

import br.com.basis.abaco.domain.Grupo;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.service.dto.GrupoDTO;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {})
public interface GrupoMapper {

    GrupoDTO grupoToGrupoDTO(Grupo grupo);

    List<GrupoDTO> gruposToGrupoDTOs(List<Grupo> grupos);

    Grupo grupoDTOToGrupo(GrupoDTO grupoDTO);

    List<Grupo> gruposDTOsToGrupos(List<GrupoDTO> grupoDTOs);

    default Set<String> stringFromUsers(Set<User> users) {
        return users.stream().map(user -> user.getFirstName().concat(" ").concat(user.getLastName())).collect(Collectors.toSet());
    }

    default Set<User> userFromString(Set<String> users) {
        return users.stream().map(name -> {
            User user = new User();
            user.setFirstName(name.substring(0, name.indexOf(' ')));
            user.setLastName(name.substring(name.indexOf(' ') + 1));
            return user;
        }).collect(Collectors.toSet());
    }

}
