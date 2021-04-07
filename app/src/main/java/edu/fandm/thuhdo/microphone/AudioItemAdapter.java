package edu.fandm.thuhdo.microphone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class AudioItemAdapter extends RecyclerView.Adapter<AudioItemAdapter.ViewHolder> {

    private Context ctx;
    private List<String> fileNames;

    public AudioItemAdapter(Context ctx, List<String> fileNames) {
        this.ctx = ctx;
        this.fileNames = fileNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.audio_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String fileName = fileNames.get(position);
        holder.bind(fileName);
    }

    @Override
    public int getItemCount() {
        return fileNames.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView fileNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.fileName);
        }

        public void bind(String fileName) {
            // bind the post data to the view element
            fileNameTextView.setText(fileName);
        }
    }
}
