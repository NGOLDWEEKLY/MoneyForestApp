package my.edu.utar.moneyforest.challenge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import my.edu.utar.moneyforest.R;

/*Done by Ng Jing Ying*/
/*This is an adapter class used to assist ChallengeActivity to display
 * all the details of an NPC, including happiness level. */
public class NpcGridAdapter extends RecyclerView.Adapter<NpcGridAdapter.ViewHolder> {

    private List<Challenge.NPCHappiness> items;

    public NpcGridAdapter(List<Challenge.NPCHappiness> items) {
        this.items = items;
    }

    @Override
    public NpcGridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ll_npc_layout, parent, false);
        return new NpcGridAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NpcGridAdapter.ViewHolder holder, int position) {
        Challenge.NPCHappiness happi = items.get(position);
        holder.npcNameTextView.setText(happi.getName());
        if (happi.getHappiLevel() >= 0.5)
            holder.npcAvatarImageView.setImageResource(R.drawable.smile);
        else
            holder.npcAvatarImageView.setImageResource(R.drawable.cry);
        holder.happilvlPB.setProgress((int) (happi.getHappiLevel() * 100));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView npcAvatarImageView;
        TextView npcNameTextView;
        ProgressBar happilvlPB;

        ViewHolder(View itemView) {
            super(itemView);
            npcAvatarImageView = itemView.findViewById(R.id.npc_mood);
            npcNameTextView = itemView.findViewById(R.id.npc_name);
            happilvlPB = itemView.findViewById(R.id.happilvlPB);
        }
    }
}
