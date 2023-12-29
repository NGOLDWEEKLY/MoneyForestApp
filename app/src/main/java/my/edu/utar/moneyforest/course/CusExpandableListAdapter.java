package my.edu.utar.moneyforest.course;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import my.edu.utar.moneyforest.R;

/*Done by Wai Jia Le*/
/*Assisted by Ng Jing Ying*/
// This is the Adapter to use the expandablelist for the Courses

public class CusExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> expandableTitleList;
    private LinkedHashMap<String, List<String>> expandableDetailList;
    private View view;
    private List<Course> course_lst;

    public CusExpandableListAdapter(Context context, List<String> expandableListTitle,
                                    LinkedHashMap<String, List<String>> expandableListDetail, View view, List<Course> courses) {
        this.context = context;
        this.expandableTitleList = expandableListTitle;
        this.expandableDetailList = expandableListDetail;
        this.view = view;
        this.course_lst = new ArrayList<>();
        this.course_lst.addAll(courses);
    }

    @Override
    public int getGroupCount() {
        return this.expandableTitleList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return this.expandableDetailList.get(this.expandableTitleList.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return this.expandableTitleList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return this.expandableDetailList.get(this.expandableTitleList.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.view.getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_course_chapter, null);
            convertView.setBackgroundColor(context.getResources().getColor(R.color.medium_sea_green));

        }
        this.view.refreshDrawableState();
        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.course_chapter);
        listTitleTextView.setText(listTitle);

        ImageView complete_iv = (ImageView) convertView.findViewById(R.id.course_chapter_complete);
        ImageView onGoing_iv = (ImageView) convertView.findViewById(R.id.course_chapter_onGoing);

        //Check the progress of the course to display related icon
        for (int i = 0; i<course_lst.size(); i++){
            if (listTitle.equals(course_lst.get(i).getCourse_name())){
                if(course_lst.get(i).getCourse_progress() == 1){
                    complete_iv.setVisibility(View.VISIBLE);
                    onGoing_iv.setVisibility(View.GONE);
                }
                else if (course_lst.get(i).getCourse_progress()<1 && course_lst.get(i).getCourse_progress()>0){
                    complete_iv.setVisibility(View.GONE);
                    onGoing_iv.setVisibility(View.VISIBLE);
                }
                else{
                    complete_iv.setVisibility(View.GONE);
                    onGoing_iv.setVisibility(View.GONE);
                }
            }
        }
        return convertView;
    }

    @Override
    public View getChildView(int lstPosn, int expanded_ListPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(lstPosn, expanded_ListPosition);
        Boolean checkLocked = false;
        System.out.println("lstPosn: " + lstPosn + "expanded: " + expanded_ListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_course_title, null);

        }
        TextView expandedListTextView = (TextView) convertView.findViewById(R.id.course_title);
        expandedListTextView.setText(expandedListText);
        //set the lock icon for the locked courses
        ImageView section_type_iv = convertView.findViewById(R.id.section_type_iv);
        ImageView lock = convertView.findViewById(R.id.course_title_lock);
        ImageView unlock = convertView.findViewById(R.id.course_title_unlock);
        ImageView course_title_done = convertView.findViewById(R.id.course_title_done);

        //Check the progress of the current chapter to show the related icons
        for (int i = 0; i < course_lst.get(lstPosn).getCourse_sections().size(); i++) {
            if (expandedListText.equals(course_lst.get(lstPosn).getCourse_sections().get(i).getName())) {

                if(course_lst.get(lstPosn).getCourse_sections().get(i).isIs_chal())
                    section_type_iv.setImageResource(R.drawable.challenge);
                else
                    section_type_iv.setImageResource(R.drawable.ic_coin);
                long status = course_lst.get(lstPosn).getCourse_sections().get(i).getStatus();

                if (status == Course.SECTION_LOCKED) {
                    lock.setVisibility(View.VISIBLE);
                    unlock.setVisibility(View.GONE);
                    course_title_done.setVisibility(View.GONE);
                    expandedListTextView.setTextColor(context.getResources().getColor(R.color.dark_gray));
                    section_type_iv.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), android.graphics.PorterDuff.Mode.SRC_IN);
                } else if (status == Course.SECTION_UNLOCKED) {
                    lock.setVisibility(View.GONE);
                    unlock.setVisibility(View.VISIBLE);
                    course_title_done.setVisibility(View.GONE);
                    expandedListTextView.setTextColor(ContextCompat.getColor(context, R.color.medium_sea_green));
                    section_type_iv.setColorFilter(ContextCompat.getColor(context, R.color.medium_sea_green), android.graphics.PorterDuff.Mode.SRC_IN);
                } else if (status == Course.SECTION_DONE) {
                    lock.setVisibility(View.GONE);
                    unlock.setVisibility(View.GONE);
                    course_title_done.setVisibility(View.VISIBLE);
                    expandedListTextView.setTextColor(ContextCompat.getColor(context, R.color.medium_sea_green));
                    section_type_iv.setColorFilter(ContextCompat.getColor(context, R.color.medium_sea_green), android.graphics.PorterDuff.Mode.SRC_IN);

                }
            }
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
