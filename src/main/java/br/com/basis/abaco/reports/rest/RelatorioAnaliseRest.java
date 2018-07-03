package br.com.basis.abaco.reports.rest;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.basis.abaco.domain.Alr;
import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Der;
import br.com.basis.abaco.domain.FatorAjuste;
import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.Rlr;
import br.com.basis.abaco.domain.enumeration.ImpactoFatorAjuste;
import br.com.basis.abaco.reports.util.RelatorioUtil;
import br.com.basis.abaco.service.dto.AlrFtDTO;
import br.com.basis.abaco.service.dto.DerFdDTO;
import br.com.basis.abaco.service.dto.DerFtDTO;
import br.com.basis.abaco.service.dto.FuncaoDadosDTO;
import br.com.basis.abaco.service.dto.FuncaoTransacaoDTO;
import br.com.basis.abaco.service.dto.FuncoesDTO;
import br.com.basis.abaco.service.dto.RlrFdDTO;
import net.sf.jasperreports.engine.JRException;

/**
 * @author eduardo.andrade
 * @since 27/04/2018
 */
public class RelatorioAnaliseRest {

    private static String caminhoRalatorioAnalise = "reports/analise/analise.jasper";
    
    private static String caminhoAnaliseDetalhada = "reports/analise/analise_detalhada.jasper";
    
    private static String caminhoImagem = "reports/img/fnde_logo.png";

    private HttpServletRequest request;
    
    private HttpServletResponse response;

    private Analise analise;
    
    private RelatorioUtil relatorio;

    private Map<String, Object> parametro;
    
    private RelatorioFuncoes relatorioFuncoes;
    
    private List<FuncoesDTO> listFuncoes;
    
    private static String deflator = " Deflator em Projetos de Melhoria";

    public RelatorioAnaliseRest(HttpServletResponse response, HttpServletRequest request ) {
        this.response = response;
        this.request = request;
    }

    /**
     * 
     */
    private void init() {
        listFuncoes = new ArrayList<FuncoesDTO>();
        analise = new Analise();
        relatorio = new RelatorioUtil( this.response, this.request);
        relatorioFuncoes = new RelatorioFuncoes();
    }

    /**
     * 
     * @param analise
     */
    private void popularObjeto(Analise analise) {
        this.analise = analise;
    }

    /**
     * 
     * @param analise
     * @throws FileNotFoundException
     * @throws JRException
     */
    public ResponseEntity<byte[]> downloadPdfArquivo(Analise analise) throws FileNotFoundException, JRException {
        init();
        popularObjeto(analise);
        return relatorio.downloadPdfArquivo(analise, caminhoRalatorioAnalise, popularParametroAnalise());
    }
    
    /**
     * 
     * @param analise
     * @throws FileNotFoundException
     * @throws JRException
     */
    public @ResponseBody byte[] downloadPdfBrowser(Analise analise) throws FileNotFoundException, JRException {
        init();
        popularObjeto(analise);
        return relatorio.downloadPdfBrowser(analise, caminhoRalatorioAnalise, popularParametroAnalise());
    }

    /**
     * Método responsável por popular o parametro do Jasper.
     */
    private Map<String, Object> popularParametroAnalise() {
        parametro = new HashMap<String, Object>();
        this.popularImagemRelatorio();
        this.popularUsuarios();
        this.popularDadosGerais();
        this.popularContrato();
        this.popularOrganizacao();
        this.popularSistema();
        this.popularManual();
        this.popularResumo();
        this.popularDadosBasicos();
        this.popularFuncao();
        this.popularListaParametro();
        this.popularListas();
        this.popularAjustes();
        this.popularCountsFd();
        this.popularCountsFt();
        return parametro;
    }

    /**
     * 
     */
    private void popularUsuarios() {
        if(validarObjetosNulos(analise.getCreatedBy())) {
            parametro.put("CRIADOPOR", analise.getCreatedBy().getLogin());            
        }
        if(validarObjetosNulos(analise.getEditedBy())) {
            parametro.put("EDITADOPOR", analise.getEditedBy().getLogin());            
        }
    }

    /**
     * Método responsável por acessar o caminho da imagem da logo do relatório e popular o parâmetro.
    */
    private void popularImagemRelatorio() {
        InputStream reportStream = getClass().getClassLoader().getResourceAsStream(caminhoImagem);
        parametro.put("IMAGEMLOGO", reportStream);
    }
    
    /**
     * Método responsável por popular as informações Gerais do relatório.
     */
    private void popularDadosGerais() {
        parametro.put("EQUIPE", analise.getEquipeResponsavel().getNome());
        parametro.put("IDENTIFICADOR", analise.getIdentificadorAnalise());
        parametro.put("TIPOANALISE", analise.getTipoAnalise().toString());
        parametro.put("GARANTIA", garantia());
        parametro.put("DATAHMG", formatarData(analise.getDataHomologacao()));
        parametro.put("NUMEROOS", analise.getNumeroOs());
        parametro.put("PROPOSITO", analise.getPropositoContagem());
        parametro.put("ESCOPO", analise.getEscopo());
        parametro.put("FRONTEIRA", analise.getFronteiras());
        parametro.put("DOCUMENTACAO", analise.getDocumentacao());
        parametro.put("OBSERVACOES", analise.getObservacoes());
    }
    
    /**
     * 
     */
    private void popularContrato() {
        if (validarObjetosNulos(analise.getContrato())) {
            parametro.put("CONTRATO", analise.getContrato().getNumeroContrato());
            parametro.put("CONTRATODTINICIO", analise.getContrato().getDataInicioVigencia().toString());
            parametro.put("CONTRATODTFIM", analise.getContrato().getDataFimVigencia().toString());
            parametro.put("CONTRATOGARANTIA", analise.getContrato().getDiasDeGarantia().toString());
            parametro.put("CONTRATOATIVO", verificarCondicao(analise.getContrato().getAtivo()));
        }
    }

    /**
     * 
     */
    private void popularOrganizacao() {
        if(validarObjetosNulos(analise.getContrato().getOrganization())) {
            parametro.put("ORGANIZACAO", analise.getContrato().getOrganization().getSigla());
            parametro.put("ORGANIZACAONM", analise.getContrato().getOrganization().getNome()); 
        }
    }

    /**
     * 
     */
    private void popularSistema() {
        if (validarObjetosNulos(analise.getSistema())) {
            parametro.put("SISTEMASG", analise.getSistema().getSigla());
            parametro.put("SISTEMANM", analise.getSistema().getNome());
        }
    }

    /**
     * 
     */
    private void popularManual() {
        if (validarObjetosNulos(analise.getContrato()) && validarObjetosNulos(analise.getContrato().getManual())) {
            parametro.put("MANUALNM", analise.getContrato().getManual().getNome());
            parametro.put("METODOCONTAGEM", analise.getMetodoContagem().toString());
            parametro.put("VERSAOCPM", verificarVersaoCPM(analise.getContrato().getManual().getVersaoCPM()));
        }
    }
    
    /**
     * Método responsável por popular as informações do resumo da análise.
     */
    private void popularResumo() {
        parametro.put("PFTOTAL", analise.getPfTotal());
        parametro.put("AJUSTESPF", calcularPFsAjustado(analise.getPfTotal(), analise.getAdjustPFTotal()));
        parametro.put("PFAJUSTADO", analise.getAdjustPFTotal());
    }
    
    /**
     * 
    */
    private void popularDadosBasicos() {
        parametro.put("DATACRIADO", analise.getAudit().getCreatedOn().toString());
        parametro.put("DATAALTERADO", analise.getAudit().getUpdatedOn().toString());
        parametro.put("VALORAJUSTE", String.valueOf(analise.getValorAjuste()));
        parametro.put("FATORAJUSTE", verificarFatorAjuste(analise.getFatorAjuste()));
    }
    
    /**
     * 
     */
    private void popularFuncao() {
        for (FuncoesDTO funcoesDTO : relatorioFuncoes.prepararListaFuncoes(analise)) {
            listFuncoes.add(funcoesDTO);
        }
    }
    
    /**
     * 
     */
    private void popularListaParametro() {
        List<FuncaoTransacaoDTO> listFuncaoFT = new ArrayList<>();
        List<FuncaoDadosDTO> listFuncaoFD = new ArrayList<>();

        
        for(FuncoesDTO f : listFuncoes) {
            if(f.getNomeFd() != null) {
                listFuncaoFD.add(popularObjetoFd(f));
            }
            if(f.getNomeFt() != null) {
                listFuncaoFT.add(popularObjetoFt(f));
            }
        }
        parametro.put("LISTAFUNCAOFT", listFuncaoFT);
        parametro.put("LISTAFUNCAOFD", listFuncaoFD);
    }
    
    /**
     * 
     * @return
     */
    private int countQuantidadeDerFd(Long id) {
        int total = 0;
        for(FuncaoDados fd : analise.getFuncaoDados()) {
            if(fd.getId() == id) {
                total = fd.getDers().size();
            }
        }
        return total;
    }
    
    /**
     * 
     * @return
     */
    private int countQuantidadeRlrFd(Long id) {
        int total = 0;
        for(FuncaoDados fd : analise.getFuncaoDados()) {
            if(fd.getId() == id) {
                total = fd.getRlrs().size();
            }
        }
        return total;
    }
    
    /**
     * 
     * @return
     */
    private int countQuantidadeFtrFt(Long id) {
        int total = 0;
        for(FuncaoTransacao ft : analise.getFuncaoTransacaos()) {
            if(ft.getId() == id) {
                total = ft.getAlrs().size();
            }
        }
        return total;
    }
    
    /**
     * 
     * @return
     */
    private int countQuantidadeDerFt(Long id) {
        int total = 0;
        for(FuncaoTransacao ft : analise.getFuncaoTransacaos()) {
            if(ft.getId() == id) {
                total = ft.getDers().size();
            }
        }
        return total;
    }
    
    /**
     * Método responsável por invocar os métodos que populam as listas 
     * DER função de transação, 
     * ARL função de transação, 
     * DER função de dados, 
     * RLR função de dados, 
     * função de transação e função de dados.
     */
    private void popularListas() {
        this.popularListaDerFt();
        this.popularListaArlFt();
        this.popularListaDerFd();
        this.popularListaRlrFd();
        this.popularListaFuncaoTransacao();
        this.popularListaFuncaoDados();
        
    }
    
    /**
     * 
     */
    private void popularListaFuncaoTransacao() {
        List<FuncaoTransacaoDTO> listFuncaoFT = new ArrayList<>();
        
        for(FuncoesDTO f : listFuncoes) {
            if(f.getNomeFt() != null) {
                listFuncaoFT.add(popularObjetoFt(f));
            }
        }
        parametro.put("LISTAFUNCAOFT", listFuncaoFT);
    }
    
    /**
     * 
     */
    private void popularListaFuncaoDados() {
        List<FuncaoDadosDTO> listFuncaoFD = new ArrayList<>();
        
        for(FuncoesDTO f : listFuncoes) {
            if(f.getNomeFd() != null) {
                listFuncaoFD.add(popularObjetoFd(f));
            }
        }
        parametro.put("LISTAFUNCAOFD", listFuncaoFD);
    }
    
    /**
     * Método responsável por popular a lista RLR função de dados.
     */
    private void popularListaRlrFd() {
        List<RlrFdDTO> listRlrFD = new ArrayList<>();
        
        for(FuncaoDados fd : analise.getFuncaoDados()) {
            
            for(Rlr rlr : fd.getRlrs()) {
                RlrFdDTO objeto = new RlrFdDTO();

                if(rlr.getNome() != null) {
                    objeto.setNome(rlr.getNome());
                    listRlrFD.add(objeto);
                }
            }
        }
        parametro.put("LISTARLRFD", listRlrFD);
    }

    /**
     * Método responsável por popular a lista DER função de dados.
     */ 
    private void popularListaDerFd() {
        List<DerFdDTO> listDerFD = new ArrayList<>();
        
        for(FuncaoDados fd : analise.getFuncaoDados()) {
            
            for(Der der : fd.getDers()) {
                DerFdDTO objeto = new DerFdDTO();

                if(der.getNome() != null) {
                    objeto.setNome(der.getNome());
                    listDerFD.add(objeto);
                }
            }
        }
        parametro.put("LISTADERFD", listDerFD);
    }
    
    /**
     * Método responsável por popular a lista ARL função de transação.
     */
    private void popularListaArlFt() {
        List<AlrFtDTO> listArlFT = new ArrayList<>();
        
        for(FuncaoTransacao ft : analise.getFuncaoTransacaos()) {
            
            for(Alr alr : ft.getAlrs()) {
                AlrFtDTO objeto = new AlrFtDTO();
                
                if(alr.getNome() != null) {
                    objeto.setNome(alr.getNome());
                    listArlFT.add(objeto);
                }
            }
        }
        parametro.put("LISTAARLFT", listArlFT);
    }
    
    /**
     * Método responsável por popular a lista DER função de transação.
     */
    private void popularListaDerFt() {
        List<DerFtDTO> listDerFT = new ArrayList<>();
        
        for(FuncaoTransacao ft : analise.getFuncaoTransacaos()) {
            for(Der der : ft.getDers()) {
                DerFtDTO objeto = new DerFtDTO();
                
                if(der.getNome() != null) {
                    objeto.setNome(der.getNome());
                    listDerFT.add(objeto);
                }
            }
        }
        parametro.put("LISTADERFT", listDerFT);
    }
    
    /**
     * 
     * @param f
     * @return
     */
    private FuncaoDadosDTO popularObjetoFd(FuncoesDTO f) {
        FuncaoDadosDTO fd = new FuncaoDadosDTO();
        fd.setNomeFd(f.getNomeFd());
        fd.setClassificacaoFd(f.getTipoFd());
        fd.setImpactoFd(f.getImpactoFd());
        fd.setRlrFd(f.getRlrFd());
        fd.setDerFd(f.getDerFd());
        fd.setComplexidadeFd(f.getComplexidadeFd());
        fd.setPfTotalFd(f.getPfTotalFd());
        fd.setPfAjustadoFd(f.getPfAjustadoFd());
        fd.setDerFd(Integer.toString(this.countQuantidadeDerFd(f.getIdFd())));
        fd.setRlrFd(Integer.toString(this.countQuantidadeRlrFd(f.getIdFd())));
        fd.setPfTotalFd(f.getPfTotalFd());
        fd.setPfAjustadoFd(f.getPfAjustadoFd());
        return fd;
    }
    
    /**
     * 
     * @param f
     * @return
     */
    private FuncaoTransacaoDTO popularObjetoFt(FuncoesDTO f) {
        FuncaoTransacaoDTO ft = new FuncaoTransacaoDTO();
        ft.setNomeFt(f.getNomeFt());
        ft.setClassificacaoFt(f.getTipoFt());
        ft.setImpactoFt(f.getImpactoFt());
        ft.setFtrFt(f.getFtrFt());
        ft.setDerFt(f.getDerFt());
        ft.setComplexidadeFt(f.getComplexidadeFt());
        ft.setPfTotalFt(f.getPfTotalFt());
        ft.setPfAjustadoFt(f.getPfAjustadoFt());
        ft.setDerFt(Integer.toString(this.countQuantidadeDerFt(f.getIdFt())));
        ft.setFtrFt(Integer.toString(this.countQuantidadeFtrFt(f.getIdFt())));
        ft.setPfTotalFt(f.getPfTotalFt());
        ft.setPfAjustadoFt(f.getPfAjustadoFt());
        return ft;        
    }
    
    /**
     * Método responsável por popular os parâmetros de ajustes para relatório detalhado.
     */
    private void popularAjustes() {
        parametro.put("AJUSTESINCLUSAO",  funcao(ImpactoFatorAjuste.INCLUSAO.toString()) + deflator + " - Funções incluídas");
        parametro.put("AJUSTESALTERACAO",  funcao(ImpactoFatorAjuste.ALTERACAO.toString()) + deflator + " - Funções alteradas");
        parametro.put("AJUSTESEXCLUSAO",  funcao(ImpactoFatorAjuste.EXCLUSAO.toString()) + deflator + " - Funções excluídas");
        parametro.put("AJUSTESCONVERSAO",  funcao(ImpactoFatorAjuste.CONVERSAO.toString()) + deflator + " - Funções convertidas");
    }

    /**
     * 
     */
    private void popularCountsFd() {
        FuncoesDTO fd = relatorioFuncoes.recuperarCountsFd(analise);
        this.popularComplexidadeAli(fd);
        this.popularComplexidadeAie(fd);
        this.popularComplexidadeInmFd(fd);
        this.popularImpactoAli(fd);
        this.popularImpactoAie(fd);
        this.popularImpactoInmFd(fd);
    }

    /**
     * 
     */
    private void popularCountsFt() {
        FuncoesDTO ft = relatorioFuncoes.recuperarCountsFt(analise);
        this.popularComplexidadeEe(ft);
        this.popularComplexidadeSe(ft);
        this.popularComplexidadeCe(ft);
        this.popularComplexidadeInmFt(ft);
        this.popularImpactoEe(ft);
        this.popularImpactoSe(ft);
        this.popularImpactoCe(ft);
        this.popularImpactoInmFt(ft);
    }
    
    /**
     * 
     * @param fd
     */
    private void popularComplexidadeAli(FuncoesDTO fd) {
        parametro.put("ALISEM", transformarInteiro(fd.getComplexidadeDtoFd().getAliSem()));
        parametro.put("ALIBAIXA", transformarInteiro(fd.getComplexidadeDtoFd().getAliBaixa()));
        parametro.put("ALIMEDIA", transformarInteiro(fd.getComplexidadeDtoFd().getAliMedia()));
        parametro.put("ALIALTA", transformarInteiro(fd.getComplexidadeDtoFd().getAliAlta()));
        parametro.put("ALIQUANDIDADE", transformarInteiro(somaQuantidades(
                 fd.getComplexidadeDtoFd().getAliSem()
                ,fd.getComplexidadeDtoFd().getAliBaixa()
                ,fd.getComplexidadeDtoFd().getAliMedia()
                ,fd.getComplexidadeDtoFd().getAliAlta())));
        parametro.put("ALIPFTOTAL", transformarBigDecimal(fd.getComplexidadeDtoFd().getPfTotalAli()));
        parametro.put("ALIPFAJUSTADO", transformarBigDecimal(fd.getComplexidadeDtoFd().getPfAjustadoAli()));
    }

    /**
     * 
     * @param fd
     */
    private void popularComplexidadeAie(FuncoesDTO fd) {
        parametro.put("AIESEM", transformarInteiro(fd.getComplexidadeDtoFd().getAieSem()));
        parametro.put("AIEBAIXA", transformarInteiro(fd.getComplexidadeDtoFd().getAieBaixa()));
        parametro.put("AIEMEDIA", transformarInteiro(fd.getComplexidadeDtoFd().getAieMedia()));
        parametro.put("AIEALTA", transformarInteiro(fd.getComplexidadeDtoFd().getAieAlta()));
        parametro.put("AIEQUANTIDADE", transformarInteiro(somaQuantidades(
                 fd.getComplexidadeDtoFd().getAieSem()
                ,fd.getComplexidadeDtoFd().getAieBaixa()
                ,fd.getComplexidadeDtoFd().getAieMedia()
                ,fd.getComplexidadeDtoFd().getAieAlta())));
        parametro.put("AIEPFTOTAL", transformarBigDecimal(fd.getComplexidadeDtoFd().getPfTotalAie()));
        parametro.put("AIEPFAJUSTADO", transformarBigDecimal(fd.getComplexidadeDtoFd().getPfAjustadoAie()));
    }

    /**
     * 
     * @param fd
     */
    private void popularComplexidadeInmFd(FuncoesDTO fd) {
        parametro.put("INMSEM", transformarInteiro(fd.getComplexidadeDtoFd().getInmSemFd()));
        parametro.put("INMBAIXA", transformarInteiro(fd.getComplexidadeDtoFd().getInmBaixaFd()));
        parametro.put("INMMEDIA", transformarInteiro(fd.getComplexidadeDtoFd().getInmMediaFd()));
        parametro.put("INMALTA", transformarInteiro(fd.getComplexidadeDtoFd().getInmAltaFd()));
        parametro.put("INMQUANTIDADE", transformarInteiro(somaQuantidades(
                 fd.getComplexidadeDtoFd().getInmSemFd()
                ,fd.getComplexidadeDtoFd().getInmBaixaFd()
                ,fd.getComplexidadeDtoFd().getInmMediaFd()
                ,fd.getComplexidadeDtoFd().getInmAltaFd())));
        parametro.put("INMPFTOTAL", transformarBigDecimal(fd.getComplexidadeDtoFd().getPfTotalInmFd()));
        parametro.put("INMPFAJUSTADO", transformarBigDecimal(fd.getComplexidadeDtoFd().getPfAjustadoInmFd()));
    }

    /**
     * 
     * @param fd
     */
    private void popularImpactoAli(FuncoesDTO fd) {
      parametro.put("ALIINCLUSAO", transformarInteiro(fd.getImpactoDtoFd().getAliInclusao()));
      parametro.put("ALIALTERACAO", transformarInteiro(fd.getImpactoDtoFd().getAliAlteracao()));
      parametro.put("ALIEXCLUSAO", transformarInteiro(fd.getImpactoDtoFd().getAliExclusao()));
      parametro.put("ALICONVERSAO", transformarInteiro(fd.getImpactoDtoFd().getAliConversao()));
    }

    /**
     * 
     * @param fd
     */
    private void popularImpactoAie(FuncoesDTO fd) {
      parametro.put("AIEINCLUSAO", transformarInteiro(fd.getImpactoDtoFd().getAieInclusao()));
      parametro.put("AIEALTERACAO", transformarInteiro(fd.getImpactoDtoFd().getAieAlteracao()));
      parametro.put("AIEEXCLUSAO", transformarInteiro(fd.getImpactoDtoFd().getAieExclusao()));
      parametro.put("AIECONVERSAO", transformarInteiro(fd.getImpactoDtoFd().getAieConversao()));
    }

    /**
     * 
     * @param fd
     */
    private void popularImpactoInmFd(FuncoesDTO fd) {
      parametro.put("INMINCLUSAO", transformarInteiro(fd.getImpactoDtoFd().getInmInclusaoFd()));
      parametro.put("INMALTERACAO", transformarInteiro(fd.getImpactoDtoFd().getInmAlteracaoFd()));
      parametro.put("INMEXCLUSAO", transformarInteiro(fd.getImpactoDtoFd().getInmExclusaoFd()));
      parametro.put("INMCONVERSAO", transformarInteiro(fd.getImpactoDtoFd().getInmConversaoFd()));
    }

    /**
     * 
     * @param ft
     */
    private void popularComplexidadeEe(FuncoesDTO ft) {
        parametro.put("EESEM", transformarInteiro(ft.getComplexidadeDtoFt().getEeSem()));
        parametro.put("EEBAIXA", transformarInteiro(ft.getComplexidadeDtoFt().getEeBaixa()));
        parametro.put("EEMEDIA", transformarInteiro(ft.getComplexidadeDtoFt().getEeMedia()));
        parametro.put("EEALTA", transformarInteiro(ft.getComplexidadeDtoFt().getEeAlta()));
        parametro.put("EEQUANTIDADE", transformarInteiro(somaQuantidades(
                 ft.getComplexidadeDtoFt().getEeSem()
                ,ft.getComplexidadeDtoFt().getEeBaixa()
                ,ft.getComplexidadeDtoFt().getEeMedia()
                ,ft.getComplexidadeDtoFt().getEeAlta())));
        parametro.put("EEPFTOTAL", transformarBigDecimal(ft.getComplexidadeDtoFt().getPfTotalEe()));
        parametro.put("EEPFAJUSTADO", transformarBigDecimal(ft.getComplexidadeDtoFt().getPfAjustadoEe()));
    }

    /**
     * 
     * @param ft
     */
    private void popularComplexidadeSe(FuncoesDTO ft) {
        parametro.put("SESEM", transformarInteiro(ft.getComplexidadeDtoFt().getSeSem()));
        parametro.put("SEBAIXA", transformarInteiro(ft.getComplexidadeDtoFt().getSeBaixa()));
        parametro.put("SEMEDIA", transformarInteiro(ft.getComplexidadeDtoFt().getSeMedia()));
        parametro.put("SEALTA", transformarInteiro(ft.getComplexidadeDtoFt().getSeAlta()));
        parametro.put("SEQUANTIDADE", transformarInteiro(somaQuantidades(
                 ft.getComplexidadeDtoFt().getSeSem()
                ,ft.getComplexidadeDtoFt().getSeBaixa()
                ,ft.getComplexidadeDtoFt().getSeMedia()
                ,ft.getComplexidadeDtoFt().getSeAlta())));
        parametro.put("SEPFTOTAL", transformarBigDecimal(ft.getComplexidadeDtoFt().getPfTotalSe()));
        parametro.put("SEPFAJUSTADO", transformarBigDecimal(ft.getComplexidadeDtoFt().getPfAjustadoSe()));
    }

    /**
     * 
     * @param ft
     */
    private void popularComplexidadeCe(FuncoesDTO ft) {
        parametro.put("CESEM", transformarInteiro(ft.getComplexidadeDtoFt().getCeSem()));
        parametro.put("CEBAIXA", transformarInteiro(ft.getComplexidadeDtoFt().getCeBaixa()));
        parametro.put("CEMEDIA", transformarInteiro(ft.getComplexidadeDtoFt().getCeMedia()));
        parametro.put("CEALTA", transformarInteiro(ft.getComplexidadeDtoFt().getCeAlta()));
        parametro.put("CEQUANTIDADE", transformarInteiro(somaQuantidades(
                 ft.getComplexidadeDtoFt().getCeSem()
                ,ft.getComplexidadeDtoFt().getCeBaixa()
                ,ft.getComplexidadeDtoFt().getCeMedia()
                ,ft.getComplexidadeDtoFt().getCeAlta())));
        parametro.put("CEPFTOTAL", transformarBigDecimal(ft.getComplexidadeDtoFt().getPfTotalCe()));
        parametro.put("CEPFAJUSTADO", transformarBigDecimal(ft.getComplexidadeDtoFt().getPfAjustadoCe()));
    }

    /**
     * 
     * @param ft
     */
    private void popularComplexidadeInmFt(FuncoesDTO ft) {
        parametro.put("INMFTSEM", transformarInteiro(ft.getComplexidadeDtoFt().getInmSemFt()));
        parametro.put("INMFTBAIXA", transformarInteiro(ft.getComplexidadeDtoFt().getInmBaixaFt()));
        parametro.put("INMFTMEDIA", transformarInteiro(ft.getComplexidadeDtoFt().getInmMediaFt()));
        parametro.put("INMFTALTA", transformarInteiro(ft.getComplexidadeDtoFt().getInmAltaFt()));
        parametro.put("INMFTQUANTIDADE", transformarInteiro(somaQuantidades(
                 ft.getComplexidadeDtoFt().getInmSemFt()
                ,ft.getComplexidadeDtoFt().getInmBaixaFt()
                ,ft.getComplexidadeDtoFt().getInmMediaFt()
                ,ft.getComplexidadeDtoFt().getInmAltaFt())));
        parametro.put("INMFTPFTOTAL", transformarBigDecimal(ft.getComplexidadeDtoFt().getPfTotalInmFt()));
        parametro.put("INMFTPFAJUSTADO", transformarBigDecimal(ft.getComplexidadeDtoFt().getPfAjustadoInmFt()));
    }

    /**
     * 
     * @param ft
     */
    private void popularImpactoEe(FuncoesDTO ft) {
        parametro.put("EEINCLUSAO", transformarInteiro(ft.getImpactoDtoFt().getEeInclusao()));
        parametro.put("EEALTERACAO", transformarInteiro(ft.getImpactoDtoFt().getEeAlteracao()));
        parametro.put("EEEXCLUSAO", transformarInteiro(ft.getImpactoDtoFt().getEeExclusao()));
        parametro.put("EECONVERSAO", transformarInteiro(ft.getImpactoDtoFt().getEeConversao()));
    }

    /**
     * 
     * @param ft
     */
    private void popularImpactoSe(FuncoesDTO ft) {
        parametro.put("SEINCLUSAO", transformarInteiro(ft.getImpactoDtoFt().getSeInclusao()));
        parametro.put("SEALTERACAO", transformarInteiro(ft.getImpactoDtoFt().getSeAlteracao()));
        parametro.put("SEEXCLUSAO", transformarInteiro(ft.getImpactoDtoFt().getSeExclusao()));
        parametro.put("SECONVERSAO", transformarInteiro(ft.getImpactoDtoFt().getSeConversao()));
    }

    /**
     * 
     * @param ft
     */
    private void popularImpactoCe(FuncoesDTO ft) {
        parametro.put("CEINCLUSAO", transformarInteiro(ft.getImpactoDtoFt().getCeInclusao()));
        parametro.put("CEALTERACAO", transformarInteiro(ft.getImpactoDtoFt().getCeAlteracao()));
        parametro.put("CEEXCLUSAO", transformarInteiro(ft.getImpactoDtoFt().getCeExclusao()));
        parametro.put("CECONVERSAO", transformarInteiro(ft.getImpactoDtoFt().getCeConversao()));
    }

    /**
     * 
     * @param ft
     */
    private void popularImpactoInmFt(FuncoesDTO ft) {
        parametro.put("INMFTINCLUSAO", transformarInteiro(ft.getImpactoDtoFt().getInmInclusaoFt()));
        parametro.put("INMFTALTERACAO", transformarInteiro(ft.getImpactoDtoFt().getInmAlteracaoFt()));
        parametro.put("INMFTEXCLUSAO", transformarInteiro(ft.getImpactoDtoFt().getInmExclusaoFt()));
        parametro.put("INMFTCONVERSAO", transformarInteiro(ft.getImpactoDtoFt().getInmConversaoFt()));
    }
    
    /**
     * 
     * @param valor
     * @return
     */
    private String funcao(String valor) {
        switch(valor) {
            case "INCLUSAO" :
                return analise.getContrato().getManual().getParametroInclusao().toString();
            case "ALTERACAO" : 
                return analise.getContrato().getManual().getParametroAlteracao().toString();
            case "EXCLUSAO" : 
                return analise.getContrato().getManual().getParametroExclusao().toString();
            case "CONVERSAO" :
                return analise.getContrato().getManual().getParametroConversao().toString();
        }
        return null;
    }
    
    /**
     * 
     * @param ft
     */
    private Integer somaQuantidades(Integer sem, Integer baixa, Integer media, Integer alta) {
        if(sem == null) {
            sem = 0;
        }
        if(baixa == null ) {
            baixa = 0;
        }
        if(media == null) {
            media = 0;
        }
        if(alta == null) {
            alta = 0;
        }
        return sem + baixa + media + alta;
    }

    /**
     * 
     */
    private String verificarFatorAjuste(FatorAjuste valor) {
        if(valor == null) {
            return "Nenhum";
        } else {
            return valor.getNome();
        }
    }

    /**
     * 
     */
    private String verificarVersaoCPM(Long valor) {
        if(valor == 431) {
            return "4.3.1";
        }
        if(valor == 421) {
            return "4.2.1";
        }
        return null;
    }

    /**
     * 
     */
    private String garantia() {
        if (analise.getBaselineImediatamente()) {
            return "Sim";
        } else {
            return "Não";
        }
    }

    /**
     * 
     */
    private boolean validarObjetosNulos(Object objeto) {
        return objeto == null ? false : true;
    }

    /**
     * Método responsável por formatar a data par dia/mês/ano.
     * @param data
     * @return
     */
    public String formatarData(Date data) {
        SimpleDateFormat dataFormatada = new SimpleDateFormat("dd/MM/yyyy");
        if(data != null) {
            return dataFormatada.format(data);
        } else {
            return null;
        }
    }

    /**
     * Método responsável por verificar a condição do valor,
     * valor true = Sim, valor false = Não.
     * @param valor
     * @return
     */
    private String verificarCondicao(Boolean valor) {
        return (valor) ? "Sim" : "Não";
    }

    /**
     * 
     * @param valor
     * @return
     */
    private String transformarInteiro(Integer valor) {
        if(valor == null) {
            valor = 0;
        }
        return valor.toString();
    }

    /**
     * 
     * @param valor
     * @return
     */
    private String transformarBigDecimal(Double valor) {
        if(valor == null) {
            valor = 0.0;
        }
        return valor.toString().replace(".", ",");
    }
    
    /**
     * 
     * @param valor1
     * @param valor2
     * @return
     */
    private String calcularPFsAjustado(String valor1, String valor2) {
        Double valorCalculado = 0.0;
        if(valor1 != null && valor2 != null) {
            valorCalculado = Double.parseDouble(valor1) - Double.parseDouble(valor2);
        }
        return valorCalculado.toString().replace(".", ".").substring(0,3);
    }

}

