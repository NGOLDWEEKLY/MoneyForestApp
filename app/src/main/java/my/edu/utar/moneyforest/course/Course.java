package my.edu.utar.moneyforest.course;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import my.edu.utar.moneyforest.challenge.Challenge;
/*Done by Wai Jia Le*/
/*The structure of Course */
public class Course implements Serializable {
    public final static long SECTION_DONE = 2;
    public final static long SECTION_UNLOCKED = 1;
    public final static long SECTION_LOCKED = 0;
    public final static long COURSE_DONE_FLAG = 1009;
    private int course_id;
    private String course_name;
    private double course_progress;


    public double getCourse_progress() {
        return course_progress;
    }

    public void setCourse_progress(double course_progress) {
        this.course_progress = course_progress;
    }

    private int course_lvl;

    public List<Course_Section> getCourse_sections() {
        return course_sections;
    }

    public void setCourse_sections(List<Course_Section> course_sections) {
        this.course_sections = course_sections;
    }

    private List<Course_Section> course_sections;
    private int course_challenge_id;

    private Challenge chal;

    public int getCourse_id() {
        return course_id;
    }

    public String getCourse_name() {
        return course_name;
    }

    public int getCourse_lvl() {
        return course_lvl;
    }

    public Challenge getChal() {
        return chal;
    }

    public void setChal(Challenge chal) {
        this.chal = chal;
    }

public Course(int id, String name, int lvl, int chal_id, double course_progress, String chal_name, List<Course_Section> course_sections) {
        this.course_id = id;
        this.course_name = name;
        this.course_lvl = lvl;
        this.course_progress = course_progress;
        this.course_challenge_id = chal_id;
        this.course_sections = course_sections;
    }

    HashMap<String, List<String>> expandableDetailList = new HashMap<String, List<String>>();

    public void setData(JSONArray course_JSON)
    {
        int course_count = course_JSON.length();

        try {
            for (int i = 0; i < course_count; i++) {
                String course_name = course_JSON.getJSONObject(i).getString("course_name");


                List<String> course_section_names = new ArrayList<String>();

                JSONArray course_section = new JSONArray();
                course_section = course_JSON.getJSONObject(i).getJSONArray("course_section_names");
                int course_section_count= course_section.length();
                System.out.println("Course section length:" + course_section_count);

                int course_section_count1= course_JSON.getJSONObject(i).getJSONArray("course_section_names").length();
                System.out.println("Course section length1:" + course_section_count1);
                //get course_section
                for (int j = 0 ; j < course_section_count ; j ++)
                {
                    course_section_names.add(course_section.getString(j).toString());
                   // System.out.println("Course section test:" + j + ":" + course_section.getString(j).toString());
                }
                this.expandableDetailList.put(course_name, course_section_names);

            }
        } catch (Exception e)
        {
            System.out.println("ErrorCatch:" + e);
        }

    }

    public  HashMap<String, List<String>> getData() {
        return this.expandableDetailList;
    }

    public long updateProgress(int updateSection) {

        for(int i = 0; i < course_sections.size(); i++){
            if(course_sections.get(i).getStatus() == Course.SECTION_UNLOCKED) {
                if(Integer.parseInt(course_sections.get(i).getId()) == updateSection || course_sections.get(i).isIs_chal()) { // 0 = chalengeid
                    course_sections.get(i).setStatus(Course.SECTION_DONE);
                    if (i == course_sections.size() - 1) {
                        course_progress = 1;
                        return COURSE_DONE_FLAG;
                    } else {
                        course_progress = (Integer.parseInt(course_sections.get(i).getId())+ 0.0) / course_sections.size();
                        //unlock next course_section
                        //gg
                        course_sections.get(i + 1).setStatus(Course.SECTION_UNLOCKED);
                        return 0;
                    }
                }
            }
        }
        return 0;
    }
}

