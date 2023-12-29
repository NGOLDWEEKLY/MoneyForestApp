package my.edu.utar.moneyforest.challenge;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import my.edu.utar.moneyforest.R;

/*Done by Ng Jing Ying*/
/*This is the adapter for displaying challenge grid list in recycler view.
 * */
public class ChalGridAdapter extends RecyclerView.Adapter<ChalGridAdapter.ViewHolder> {

    private List<UserChallenge> items;
    private Context context;
    private DatabaseReference databaseReference;

    public ChalGridAdapter(List<UserChallenge> items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_challenge, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserChallenge itemUC = items.get(position);
        Challenge item = itemUC.getChallenge();
        holder.textName.setText(item.getName());
        holder.textId.setText(item.getDesc());
        switch (item.getProgress()) {
            case Challenge.CHALLENGE_NEW:
                holder.startBtn.setText("START");
                holder.redoBtn.setVisibility(View.GONE);
                break;
            case Challenge.CHALLENGE_COMPLETE:
                holder.startBtn.setTextColor(context.getResources().getColor(R.color.pale_green));
                holder.startBtn.setBackgroundColor(context.getResources().getColor(R.color.medium_sea_green));
                holder.startBtn.setText("REVIEW");
                break;
            default:
                holder.redoBtn.setVisibility(View.GONE);
                holder.startBtn.setText("RESUME");
        }
        holder.redoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertBuilder;
                AlertDialog alertBox;
                alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setTitle(item.getName());
                alertBuilder.setMessage("Are you sure you want to reset your progress?");
                alertBuilder.setCancelable(false);
                alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(context, ChallengeActivity.class);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        // Update firebase to reset the challenge history
                        databaseReference = database.getReference("user_challenge").child(itemUC.getUser().getId()).child("" + (item.getId() - 1));
                        item.setChalHistory("{}");
                        item.setProgress(0);
                        databaseReference.setValue(item);
                        intent.putExtra("userChal", itemUC);
                        intent.putExtra("type", "chal");
                        context.startActivity(intent);

                    }
                });

                alertBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                alertBox = alertBuilder.create();
                alertBox.show();
            }
        });
        holder.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChallengeActivity.class);
                intent.putExtra("userChal", itemUC);
                intent.putExtra("type", "chal");
                context.startActivity(intent);
            }
        });

        String base64Image = item.getChalImg();
        // Decode Base64 string
        if (!base64Image.isEmpty()) {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.imageChal.setImageBitmap(decodedBitmap);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textId;
        Button startBtn;
        ImageButton redoBtn;
        ImageView imageChal;

        public ViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textId = itemView.findViewById(R.id.textDesc);
            startBtn = itemView.findViewById(R.id.startBtn);
            redoBtn = itemView.findViewById(R.id.redoBtn);
            imageChal = itemView.findViewById(R.id.imageChal);
        }
    }
}
