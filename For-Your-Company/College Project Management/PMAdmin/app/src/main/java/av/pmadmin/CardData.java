package av.pmadmin;

public class CardData {

    private String gid;
    private String proj_title;
    private String proj_lang;
    private String mem1;
    private String status;

    public CardData(String gid, String proj_title, String proj_lang, String mem1, String status) {
        this.gid = gid;
        this.proj_title = proj_title;
        this.proj_lang = proj_lang;
        this.mem1 = mem1;
        this.status = status;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getProj_title() {
        return proj_title;
    }

    public void setProj_title(String proj_title) {
        this.proj_title = proj_title;
    }

    public String getProj_lang() {
        return proj_lang;
    }

    public void setProj_lang(String proj_lang) {
        this.proj_lang = proj_lang;
    }

    public String getMem1() {
        return mem1;
    }

    public void setMem1(String mem1) {
        this.mem1 = mem1;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
