package com.example.meeting_android.activity.meeting;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_android.R;

import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.List;

public class SurfaceRendererAdapter extends RecyclerView.Adapter<SurfaceRendererAdapter.RendererViewHolder> {
    private List<String> users; // List of user IDs or other identifiers
    private static final String TAG = "웹소켓";
    public EglBase.Context eglBaseContext;
    public SurfaceRendererAdapter(List<String> users) {
        this.users = users;
    }

    @Override
    public RendererViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surface_renderer, parent, false);
        return new RendererViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RendererViewHolder holder, int position) {
        // Initialize and set up the SurfaceViewRenderer for each user
        String userId = users.get(position);
        // Here you can set up the SurfaceViewRenderer for this user using userId
        SurfaceViewRenderer renderer = holder.surfaceRenderer;
        // Set renderer properties and add it to the appropriate view hierarchy
        // Example: renderer.init(context, null);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void addUser(String userId) {
        users.add(userId);
        notifyItemInserted(users.size() - 1);
    }

    public void removeUser(int position) {
        users.remove(position);
        notifyItemRemoved(position);
    }

    class RendererViewHolder extends RecyclerView.ViewHolder {
        SurfaceViewRenderer surfaceRenderer;

        RendererViewHolder(View itemView) {
            super(itemView);
            surfaceRenderer = itemView.findViewById(R.id.surfaceRenderer);
        }
    }

    void initSurfaceViewRenderer(SurfaceViewRenderer view){
        view.setMirror(false);
        //뷰의 크기에 맞게 비디오, 프레임의 크기 조정 가로 세로 비율을 유지, 검은 색 테두리가 표시 될 수 있습니다.
        view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        view.removeFrameListener(new EglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap bitmap) {
                Log.i(TAG,"removeFxrameListener");
            }
        });
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                Log.i(TAG,"onViewAttachedToWindow");

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                Log.i(TAG,"onViewDetachedFromWindow");

            }
        });
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

            }
        });
        view.init(eglBaseContext,  new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {
                Log.i(TAG,"onFirstFrameRendered");
            }
            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {
                Log.i(TAG,"onFrameResolutionChanged");
            }
        });
    }
}
