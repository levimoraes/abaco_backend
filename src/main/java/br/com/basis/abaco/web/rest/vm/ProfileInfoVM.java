package br.com.basis.abaco.web.rest.vm;

public final class ProfileInfoVM {

    private String[] activeProfiles;

    private String ribbonEnv;

    public ProfileInfoVM(String[] activeProfiles, String ribbonEnv) {
        this.activeProfiles = activeProfiles.clone();
        this.ribbonEnv = ribbonEnv;
    }

    public String[] getActiveProfiles() {
        String[] profileAux;
        profileAux = activeProfiles;
        return profileAux;
    }

    public String getRibbonEnv() {
        return ribbonEnv;
    }
}
