package my.edu.utar.moneyforest.course;

import java.io.Serializable;

/*Done By Wai Jia Le*/
//Store the data that is related to the course section (chapter)
public class Course_Section implements Serializable
{
    private String id;
    private String name;
    private boolean is_chal;
    private Long status;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIs_chal() {
        return is_chal;
    }

    public void setIs_chal(boolean is_chal) {
        this.is_chal = is_chal;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

}