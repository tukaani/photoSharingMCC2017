package com.appspot.mccfall2017g12.photoorganizer;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public abstract class RelayPostExecutionTask<Params, Progress, Result>
        extends AsyncTask<Params, Progress, Result> {

    private WeakReference<PostExecutor<Result>> postExecutorWeakReference;

    public RelayPostExecutionTask<Params, Progress, Result> with(PostExecutor<Result> postExecutor) {
        this.postExecutorWeakReference = new WeakReference<>(postExecutor);
        return this;
    }

    @Override
    protected void onPostExecute(Result result) {
        if (this.postExecutorWeakReference == null)
            return;

        PostExecutor<Result> postExecutor = this.postExecutorWeakReference.get();

        if (postExecutor != null)
            postExecutor.onPostExecute(result);
    }
}
