package br.com.basis.abaco.web.rest;

import br.com.basis.abaco.domain.Alr;
import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Compartilhada;
import br.com.basis.abaco.domain.Der;
import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.FuncaoDadosVersionavel;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.Rlr;
import br.com.basis.abaco.domain.Sistema;
import br.com.basis.abaco.domain.TipoEquipe;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.domain.enumeration.TipoRelatorio;
import br.com.basis.abaco.reports.rest.RelatorioAnaliseRest;
import br.com.basis.abaco.repository.AnaliseRepository;
import br.com.basis.abaco.repository.CompartilhadaRepository;
import br.com.basis.abaco.repository.FuncaoDadosRepository;
import br.com.basis.abaco.repository.FuncaoDadosVersionavelRepository;
import br.com.basis.abaco.repository.FuncaoTransacaoRepository;
import br.com.basis.abaco.repository.TipoEquipeRepository;
import br.com.basis.abaco.repository.UserRepository;
import br.com.basis.abaco.repository.search.AnaliseSearchRepository;
import br.com.basis.abaco.repository.search.UserSearchRepository;
import br.com.basis.abaco.security.AuthoritiesConstants;
import br.com.basis.abaco.security.SecurityUtils;
import br.com.basis.abaco.service.AnaliseService;
import br.com.basis.abaco.service.dto.AnaliseDTO;
import br.com.basis.abaco.service.exception.RelatorioException;
import br.com.basis.abaco.service.relatorio.RelatorioAnaliseColunas;
import br.com.basis.abaco.utils.AbacoUtil;
import br.com.basis.abaco.utils.PageUtils;
import br.com.basis.abaco.web.rest.util.HeaderUtil;
import br.com.basis.abaco.web.rest.util.PaginationUtil;
import br.com.basis.dynamicexports.service.DynamicExportsService;
import br.com.basis.dynamicexports.util.DynamicExporter;
import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class AnaliseResource {

    private final Logger log = LoggerFactory.getLogger(AnaliseResource.class);
    private static final String ENTITY_NAME = "analise";
    private static final String PAGE = "page";
    private final AnaliseRepository analiseRepository;
    private final UserRepository userRepository;
    private final AnaliseService analiseService;
    private final CompartilhadaRepository compartilhadaRepository;
    private final AnaliseSearchRepository analiseSearchRepository;
    private final FuncaoDadosVersionavelRepository funcaoDadosVersionavelRepository;
    private final FuncaoDadosRepository funcaoDadosRepository;
    private final FuncaoTransacaoRepository funcaoTransacaoRepository;
    private RelatorioAnaliseRest relatorioAnaliseRest;
    private DynamicExportsService dynamicExportsService;
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private TipoEquipeRepository tipoEquipeRepository;
    @Autowired
    private UserSearchRepository userSearchRepository;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    public AnaliseResource(AnaliseRepository analiseRepository,
                           AnaliseSearchRepository analiseSearchRepository,
                           FuncaoDadosVersionavelRepository funcaoDadosVersionavelRepository,
                           DynamicExportsService dynamicExportsService,
                           UserRepository userRepository,
                           FuncaoDadosRepository funcaoDadosRepository,
                           CompartilhadaRepository compartilhadaRepository,
                           FuncaoTransacaoRepository funcaoTransacaoRepository,
                           ElasticsearchTemplate elasticsearchTemplate,
                           AnaliseService analiseService) {
        this.analiseRepository = analiseRepository;
        this.analiseSearchRepository = analiseSearchRepository;
        this.funcaoDadosVersionavelRepository = funcaoDadosVersionavelRepository;
        this.dynamicExportsService = dynamicExportsService;
        this.userRepository = userRepository;
        this.compartilhadaRepository = compartilhadaRepository;
        this.funcaoDadosRepository = funcaoDadosRepository;
        this.funcaoTransacaoRepository = funcaoTransacaoRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.analiseService = analiseService;
    }

    @PostMapping("/analises")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER, AuthoritiesConstants.GESTOR, AuthoritiesConstants.ANALISTA})
    public ResponseEntity<Analise> createAnalise(@Valid @RequestBody Analise analise) throws URISyntaxException {
        if (analise.getId() != null) {
            return ResponseEntity.badRequest().headers(
                    HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new analise cannot already have an ID")).body(null);
        }
        analise.setCreatedBy(userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get());
        salvaNovaData(analise);
        linkFuncoesToAnalise(analise);
        analiseRepository.save(analise);
        analiseSearchRepository.save(convertToEntity(convertToDto(analise)));
        return ResponseEntity.created(new URI("/api/analises/" + analise.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, analise.getId().toString())).body(analise);
    }

    @PutMapping("/analises")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER, AuthoritiesConstants.GESTOR, AuthoritiesConstants.ANALISTA})
    public ResponseEntity<Analise> updateAnalise(@Valid @RequestBody Analise analise) throws URISyntaxException {
        if (analise.getId() == null) {
            return createAnalise(analise);
        }
        if (analise.isBloqueiaAnalise()) {
            return ResponseEntity.badRequest().headers(
                    HeaderUtil.createFailureAlert(ENTITY_NAME, "analiseblocked", "You cannot edit an blocked analise")).body(null);
        }
        analise.setEditedBy(analiseRepository.findOne(analise.getId()).getCreatedBy());
        salvaNovaData(analise);
        linkFuncoesToAnalise(analise);
        analiseRepository.save(analise);
        analiseSearchRepository.save(convertToEntity(convertToDto(analise)));
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, analise.getId().toString()))
                .body(analise);
    }

    @PutMapping("/analises/{id}/block")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER, AuthoritiesConstants.GESTOR, AuthoritiesConstants.ANALISTA})
    public ResponseEntity<Analise> blockUnblockAnalise(@PathVariable Long id) throws URISyntaxException {
        log.debug("REST request to block Analise : {}", id);

        Analise analise = recuperarAnalise(id);

        if (analise != null) {
            linkFuncoesToAnalise(analise);
            if (analise.isBloqueiaAnalise()) {
                analise.setBloqueiaAnalise(false);
            } else {
                analise.setBloqueiaAnalise(true);
            }
            Analise result = analiseRepository.save(analise);
            analiseSearchRepository.save(convertToEntity(convertToDto(analise)));
            return ResponseEntity.ok().headers(HeaderUtil.blockEntityUpdateAlert(ENTITY_NAME, analise.getId().toString()))
                    .body(result);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Analise());
        }
    }

    @GetMapping("/analises/clonar/{id}")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER, AuthoritiesConstants.GESTOR, AuthoritiesConstants.ANALISTA})
    public ResponseEntity<Analise> cloneAnalise(@PathVariable Long id) {
        Analise analise = recuperarAnalise(id);
        if (analise.getId() != null) {
            Analise analiseClone = new Analise(analise, userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get());
            analiseClone.setIdentificadorAnalise(analise.getIdentificadorAnalise() + " - CÓPIA");
            salvaNovaData(analiseClone);
            analiseClone.setDataCriacaoOrdemServico(analise.getDataHomologacao());
            analiseClone.setFuncaoDados(bindCloneFuncaoDados(analise, analiseClone));
            analiseClone.setFuncaoTransacaos(bindCloneFuncaoTransacaos(analise, analiseClone));
            analiseRepository.save(analiseClone);
            analiseSearchRepository.save(convertToEntity(convertToDto(analise)));
            return ResponseEntity.ok().headers(HeaderUtil.blockEntityUpdateAlert(ENTITY_NAME, analiseClone.getId().toString()))
                    .body(analiseClone);
        } else {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new Analise());
        }
    }

    @GetMapping("/analises/clonar/{id}/{idEquipe}")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER, AuthoritiesConstants.GESTOR, AuthoritiesConstants.ANALISTA})
    public ResponseEntity<Analise> cloneAnaliseToEquipe(@PathVariable Long id, @PathVariable Long idEquipe) {
        Analise analise = recuperarAnalise(id);
        TipoEquipe tipoEquipe = tipoEquipeRepository.findById(idEquipe);
        if (analise.getId() != null && tipoEquipe.getId() != null && !(analise.getClonadaParaEquipe())) {
            analise.setClonadaParaEquipe(true);
            Analise analiseClone = new Analise(analise, userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get());
            bindAnaliseCloneForTipoEquipe(analise, tipoEquipe, analiseClone);
            analiseRepository.save(analiseClone);
            analiseSearchRepository.save(convertToEntity(convertToDto(analiseClone)));
            analiseRepository.save(analise);
            analiseSearchRepository.save(convertToEntity(convertToDto(analise)));
            return ResponseEntity.ok().headers(HeaderUtil.blockEntityUpdateAlert(ENTITY_NAME, analiseClone.getId().toString()))
                    .body(analiseClone);
        } else {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new Analise());
        }
    }

    @GetMapping("/analises/{id}")
    @Timed
    public ResponseEntity<Analise> getAnalise(@PathVariable Long id) {
        Analise analise = recuperarAnalise(id);
        if (analise != null) {
            User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
            if (user.getOrganizacoes().contains(analise.getOrganizacao()) && user.getTipoEquipes().contains(analise.getEquipeResponsavel())) {
                return ResponseUtil.wrapOrNotFound(Optional.ofNullable(analise));
            }
        }
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(null);

    }

    @GetMapping("/analises/baseline")
    @Timed
    public List<Analise> getAllAnalisesBaseline() {
        return analiseRepository.findAllByBaseline();
    }

    @DeleteMapping("/analises/{id}")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER, AuthoritiesConstants.GESTOR, AuthoritiesConstants.ANALISTA})
    public ResponseEntity<Void> deleteAnalise(@PathVariable Long id) {

        Analise analise = recuperarAnalise(id);

        if (analise != null) {
            analiseRepository.delete(id);
            analiseSearchRepository.delete(id);
        } else {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(null);
        }


        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/compartilhada/{idAnalise}")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER, AuthoritiesConstants.GESTOR, AuthoritiesConstants.ANALISTA})
    public List<Compartilhada> getAllCompartilhadaByAnalise(@PathVariable Long idAnalise) {
        return compartilhadaRepository.findAllByAnaliseId(idAnalise);
    }

    @PostMapping("/analises/compartilhar")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER, AuthoritiesConstants.GESTOR, AuthoritiesConstants.ANALISTA})
    public ResponseEntity<List<Compartilhada>> popularCompartilhar(@Valid @RequestBody List<Compartilhada> compartilhadaList) throws URISyntaxException {
        List<Compartilhada> result = compartilhadaRepository.save(compartilhadaList);
        return ResponseEntity.created(new URI("/api/analises/compartilhar"))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, "created")).body(result);
    }

    @DeleteMapping("/analises/compartilhar/delete/{id}")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER, AuthoritiesConstants.GESTOR, AuthoritiesConstants.ANALISTA})
    public ResponseEntity<Void> deleteCompartilharAnalise(@PathVariable Long id) {
        compartilhadaRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @PutMapping("/analises/compartilhar/viewonly/{id}")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER, AuthoritiesConstants.GESTOR, AuthoritiesConstants.ANALISTA})
    public ResponseEntity<Compartilhada> viewOnly(@Valid @RequestBody Compartilhada compartilhada) throws URISyntaxException {
        Compartilhada result = compartilhadaRepository.save(compartilhada);
        return ResponseEntity.ok().headers(HeaderUtil.blockEntityUpdateAlert(ENTITY_NAME, compartilhada.getId().toString()))
                .body(result);
    }


    @GetMapping("/relatorioPdfArquivo/{id}")
    @Timed
    public ResponseEntity<byte[]> downloadPdfArquivo(@PathVariable Long id) throws URISyntaxException, IOException, JRException {
        Analise analise = recuperarAnalise(id);
        relatorioAnaliseRest = new RelatorioAnaliseRest(this.response, this.request);
        return relatorioAnaliseRest.downloadPdfArquivo(analise, TipoRelatorio.ANALISE);
    }

    @GetMapping("/relatorioPdfBrowser/{id}")
    @Timed
    public @ResponseBody
    byte[] downloadPdfBrowser(@PathVariable Long id) throws URISyntaxException, IOException, JRException {
        Analise analise = recuperarAnalise(id);
        relatorioAnaliseRest = new RelatorioAnaliseRest(this.response, this.request);
        return relatorioAnaliseRest.downloadPdfBrowser(analise, TipoRelatorio.ANALISE);
    }

    @GetMapping("/downloadPdfDetalhadoBrowser/{id}")
    @Timed
    public @ResponseBody
    byte[] downloadPdfDetalhadoBrowser(@PathVariable Long id) throws URISyntaxException, IOException, JRException {
        Analise analise = recuperarAnalise(id);
        relatorioAnaliseRest = new RelatorioAnaliseRest(this.response, this.request);
        return relatorioAnaliseRest.downloadPdfBrowser(analise, TipoRelatorio.ANALISE_DETALHADA);
    }

    @GetMapping("/downloadRelatorioExcel/{id}")
    @Timed
    public @ResponseBody
    byte[] downloadRelatorioExcel(@PathVariable Long id) throws URISyntaxException, IOException, JRException {
        Analise analise = recuperarAnalise(id);
        relatorioAnaliseRest = new RelatorioAnaliseRest(this.response, this.request);
        return relatorioAnaliseRest.downloadExcel(analise);
    }

    @GetMapping("/relatorioContagemPdf/{id}")
    @Timed
    public @ResponseBody
    ResponseEntity<InputStreamResource> gerarRelatorioContagemPdf(@PathVariable Long id) throws IOException, JRException {
        Analise analise = recuperarAnaliseContagem(id);
        relatorioAnaliseRest = new RelatorioAnaliseRest(this.response, this.request);
        return relatorioAnaliseRest.downloadReportContagem(analise);
    }

    @GetMapping(value = "/analise/exportacao/{tipoRelatorio}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Timed
    public ResponseEntity<InputStreamResource> gerarRelatorioExportacao(@PathVariable String tipoRelatorio,
                                                                        @RequestParam(defaultValue = "ASC") String order,
                                                                        @RequestParam(defaultValue = "0", name = PAGE) int pageNumber,
                                                                        @RequestParam(defaultValue = "20") int size,
                                                                        @RequestParam(defaultValue = "id") String sort,
                                                                        @RequestParam(value = "identificador", required = false) String identificador,
                                                                        @RequestParam(value = "sistema", required = false) String sistema,
                                                                        @RequestParam(value = "metodo", required = false) String metodo,
                                                                        @RequestParam(value = "organizacao", required = false) String organizacao,
                                                                        @RequestParam(value = "equipe", required = false) String equipe,
                                                                        @RequestParam(value = "usuario", required = false) String usuario) throws RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            Pageable pageable = dynamicExportsService.obterPageableMaximoExportacao();
            Set<Long> equipesIds = getIdEquipes();
            BoolQueryBuilder qb = QueryBuilders.boolQuery();
            analiseService.bindFilterSearch(identificador, sistema, metodo, organizacao, equipe, usuario, equipesIds, qb);
            SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(qb).withPageable(pageable).build();
            Page<Analise> page = elasticsearchTemplate.queryForPage(searchQuery, Analise.class);
            byteArrayOutputStream = dynamicExportsService.export(new RelatorioAnaliseColunas(), page, tipoRelatorio, Optional.empty(), Optional.ofNullable(AbacoUtil.REPORT_LOGO_PATH), Optional.ofNullable(AbacoUtil.getReportFooter()));
        } catch (DRException | ClassNotFoundException | JRException | NoClassDefFoundError e) {
            log.error(e.getMessage(), e);
            throw new RelatorioException(e);
        }
        return DynamicExporter.output(byteArrayOutputStream,
                "relatorio." + tipoRelatorio);
    }

    @GetMapping("/analises")
    @Timed
    public ResponseEntity<List<AnaliseDTO>> getAllAnalisesEquipes(@RequestParam(defaultValue = "ASC") String order,
                                                                  @RequestParam(defaultValue = "0", name = PAGE) int pageNumber,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  @RequestParam(defaultValue = "id") String sort,
                                                                  @RequestParam(value = "identificador", required = false) String identificador,
                                                                  @RequestParam(value = "sistema", required = false) String sistema,
                                                                  @RequestParam(value = "metodo", required = false) String metodo,
                                                                  @RequestParam(value = "organizacao", required = false) String organizacao,
                                                                  @RequestParam(value = "equipe", required = false) String equipe,
                                                                  @RequestParam(value = "usuario", required = false) String usuario)
            throws URISyntaxException {
        Direction sortOrder = PageUtils.getSortDirection(order);
        Pageable pageable = new PageRequest(pageNumber, size, sortOrder, sort);
        Set<Long> equipesIds = getIdEquipes();
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        analiseService.bindFilterSearch(identificador, sistema, metodo, organizacao, equipe, usuario, equipesIds, qb);
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(qb).withPageable(pageable).build();
        Page<Analise> page = elasticsearchTemplate.queryForPage(searchQuery, Analise.class);
        Page<AnaliseDTO> dtoPage = page.map(analise -> this.convertToDto(analise));
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/analises/");
        return new ResponseEntity<>(dtoPage.getContent(), headers, HttpStatus.OK);
    }


    private Set<Long> getIdEquipes() {
        User user = userSearchRepository.findByLogin(SecurityUtils.getCurrentUserLogin());
        Set<TipoEquipe> listaEquipes = user.getTipoEquipes();
        Set<Long> equipesIds = new HashSet<>();
        listaEquipes.forEach(tipoEquipe -> {
            equipesIds.add(tipoEquipe.getId());
        });
        return equipesIds;
    }

    private Boolean checarPermissao(Long idAnalise) {
        Optional<User> logged = userRepository.findOneWithAuthoritiesByLogin(SecurityUtils.getCurrentUserLogin());
        List<BigInteger> equipesIds = userRepository.findUserEquipes(logged.get().getId());
        List<Long> convertidos = equipesIds.stream().map(bigInteger -> bigInteger.longValue()).collect(Collectors.toList());
        Integer analiseDaEquipe = analiseRepository.analiseEquipe(idAnalise, convertidos);
        if (analiseDaEquipe.intValue() == 0) {
            return verificaCompartilhada(idAnalise);
        } else {
            return true;
        }

    }

    private Boolean verificaCompartilhada(Long idAnalise) {
        return compartilhadaRepository.existsByAnaliseId(idAnalise);
    }

    private Analise recuperarAnalise(Long id) {

        boolean retorno = checarPermissao(id);

        if (retorno) {
            return analiseRepository.findById(id);
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Analise recuperarAnaliseContagem(@NotNull Long id) {

        boolean retorno = checarPermissao(id);

        if (retorno) {
            Analise analise = analiseRepository.reportContagem(id);
            Sistema sistema = analise.getSistema();
            if (sistema != null) {
                sistema.getModulos().forEach(modulo -> {
                    modulo.getFuncionalidades().forEach(funcionalidade -> {
                        funcionalidade.setFuncoesDados(funcaoDadosRepository.findByAnaliseFuncionalidade(id, funcionalidade.getId()));
                        funcionalidade.setFuncoesTransacao(funcaoTransacaoRepository.findByAnaliseFuncionalidade(id, funcionalidade.getId()));
                    });
                });
                return analise;
            }
            return null;
        } else {
            return null;
        }
    }

    private void linkFuncoesToAnalise(Analise analise) {
        linkAnaliseToFuncaoDados(analise);
        linkAnaliseToFuncaoTransacaos(analise);
    }

    private void linkAnaliseToFuncaoDados(Analise analise) {
        Optional.ofNullable(analise.getFuncaoDados()).orElse(Collections.emptySet())
                .forEach(funcaoDados -> {
                    funcaoDados.setAnalise(analise);
                    linkFuncaoDadosRelationships(funcaoDados);
                    handleVersionFuncaoDados(funcaoDados, analise.getSistema());
                });
    }

    private void linkFuncaoDadosRelationships(FuncaoDados funcaoDados) {
        Optional.ofNullable(funcaoDados.getFiles()).orElse(Collections.emptyList())
                .forEach(file -> file.setFuncaoDados(funcaoDados));
        Optional.ofNullable(funcaoDados.getDers()).orElse(Collections.emptySet())
                .forEach(der -> der.setFuncaoDados(funcaoDados));
        Optional.ofNullable(funcaoDados.getRlrs()).orElse(Collections.emptySet())
                .forEach(rlr -> rlr.setFuncaoDados(funcaoDados));
    }

    private void handleVersionFuncaoDados(FuncaoDados funcaoDados, Sistema sistema) {
        String nome = funcaoDados.getName();
        Optional<FuncaoDadosVersionavel> funcaoDadosVersionavel =
                funcaoDadosVersionavelRepository.findOneByNomeIgnoreCaseAndSistemaId(nome, sistema.getId());
        if (funcaoDadosVersionavel.isPresent()) {
            funcaoDados.setFuncaoDadosVersionavel(funcaoDadosVersionavel.get());
        } else {
            FuncaoDadosVersionavel novaFDVersionavel = new FuncaoDadosVersionavel();
            novaFDVersionavel.setNome(funcaoDados.getName());
            novaFDVersionavel.setSistema(sistema);
            FuncaoDadosVersionavel result = funcaoDadosVersionavelRepository.save(novaFDVersionavel);
            funcaoDados.setFuncaoDadosVersionavel(result);
        }
    }

    private void linkAnaliseToFuncaoTransacaos(Analise analise) {
        Optional.ofNullable(analise.getFuncaoTransacaos()).orElse(Collections.emptySet())
                .forEach(funcaoTransacao -> {
                    funcaoTransacao.setAnalise(analise);
                    Optional.ofNullable(funcaoTransacao.getFiles()).orElse(Collections.emptyList())
                            .forEach(file -> file.setFuncaoTransacao(funcaoTransacao));
                    Optional.ofNullable(funcaoTransacao.getDers()).orElse(Collections.emptySet())
                            .forEach(der -> der.setFuncaoTransacao(funcaoTransacao));
                    Optional.ofNullable(funcaoTransacao.getAlrs()).orElse(Collections.emptySet())
                            .forEach(alr -> alr.setFuncaoTransacao(funcaoTransacao));
                });
    }

    private void salvaNovaData(Analise analise) {
        if (analise.getDataHomologacao() != null) {
            Timestamp dataDeHoje = new Timestamp(System.currentTimeMillis());
            Timestamp dataParam = analise.getDataHomologacao();
            dataParam.setHours(dataDeHoje.getHours());
            dataParam.setMinutes(dataDeHoje.getMinutes());
            dataParam.setSeconds(dataDeHoje.getSeconds());
        }
    }

    private Set<FuncaoDados> bindCloneFuncaoDados(Analise analise, Analise analiseClone) {
        Set<FuncaoDados> funcaoDados = new HashSet<>();
        analise.getFuncaoDados().forEach(fd -> {
            Set<Rlr> rlrs = new HashSet<>();
            Set<Der> ders = new HashSet<>();
            FuncaoDados funcaoDado = new FuncaoDados();
            bindFuncaoDados(analiseClone, fd, rlrs, ders, funcaoDado);
            funcaoDado.setDers(ders);
            funcaoDado.setRlrs(rlrs);
            funcaoDados.add(funcaoDado);
        });
        return funcaoDados;
    }

    private Set<FuncaoTransacao> bindCloneFuncaoTransacaos(Analise analise, Analise analiseClone) {
        Set<FuncaoTransacao> funcaoTransacoes = new HashSet<>();
        analise.getFuncaoTransacaos().forEach(ft -> {
            Set<Alr> alrs = new HashSet<>();
            Set<Der> ders = new HashSet<>();
            FuncaoTransacao funcaoTransacao = new FuncaoTransacao();
            funcaoTransacao.bindFuncaoTransacao(ft.getTipo(), null, ft.getFtrStr(), ft.getQuantidade(), alrs, null, ft.getFtrValues(), ft.getImpacto(), ders, analiseClone, null, ft.getComplexidade(), ft.getPf(), ft.getGrossPF(), ft.getFuncionalidade(), ft.getDetStr(), ft.getFatorAjuste(), ft.getName(), ft.getSustantation(), ft.getDerValues(), null);
            ft.getAlrs().forEach(alr -> {
                Alr alrClone = new Alr(null, alr.getNome(), alr.getValor(), funcaoTransacao, null);
                alrs.add(alrClone);
            });
            ft.getDers().forEach(der -> {
                Der derClone = new Der(null, der.getNome(), der.getValor(), der.getRlr(), null, funcaoTransacao);
                ders.add(derClone);
            });
            funcaoTransacoes.add(funcaoTransacao);
        });
        return funcaoTransacoes;
    }

    private void bindAnaliseCloneForTipoEquipe(Analise analise, TipoEquipe tipoEquipe, Analise analiseClone) {
        analiseClone.setPfTotal("0");
        analiseClone.setAdjustPFTotal("0");
        analiseClone.setEquipeResponsavel(tipoEquipe);
        analiseClone.setUsers(new HashSet<>());
        analiseClone.setBloqueiaAnalise(false);
        analiseClone.setClonadaParaEquipe(true);
        salvaNovaData(analiseClone);
        analiseClone.setDataCriacaoOrdemServico(analise.getDataHomologacao());
    }

    private void bindFuncaoDados(Analise analiseClone, FuncaoDados fd, Set<Rlr> rlrs, Set<Der> ders, FuncaoDados funcaoDado) {
        funcaoDado.bindFuncaoDados(fd.getComplexidade(), fd.getPf(), fd.getGrossPF(), analiseClone, fd.getFuncionalidade(), fd.getDetStr(), fd.getFatorAjuste(), fd.getName(), fd.getSustantation(), fd.getDerValues(), fd.getTipo(), fd.getFuncionalidades(), fd.getRetStr(), fd.getQuantidade(), rlrs, fd.getAlr(), fd.getFiles(), fd.getRlrValues(), ders, fd.getFuncaoDadosVersionavel(), fd.getImpacto());
        Optional.ofNullable(fd.getDers()).orElse(Collections.emptySet())
                .forEach(der -> {
                    Rlr rlr = null;
                    if (der.getRlr() != null) {
                        rlr = new Rlr(null, der.getRlr().getNome(), der.getRlr().getValor(), der.getRlr().getDers(), funcaoDado);
                    }
                    Der derClone = new Der(null, der.getNome(), der.getValor(), rlr, funcaoDado, null);
                    ders.add(derClone);
                });
        Optional.ofNullable(fd.getRlrs()).orElse(Collections.emptySet())
                .forEach(rlr -> {
                    Rlr rlrClone = new Rlr(null, rlr.getNome(), rlr.getValor(), ders, funcaoDado);
                    rlrs.add(rlrClone);
                });
    }

    private AnaliseDTO convertToDto(Analise analise) {
        return new ModelMapper().map(analise, AnaliseDTO.class);
    }

    private Analise convertToEntity(AnaliseDTO analiseDTO) {
        return new ModelMapper().map(analiseDTO, Analise.class);
    }
}


