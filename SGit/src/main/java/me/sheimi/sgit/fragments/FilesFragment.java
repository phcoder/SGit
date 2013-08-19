package me.sheimi.sgit.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import org.eclipse.jgit.api.Git;

import java.io.File;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.adapters.FilesListAdapter;
import me.sheimi.sgit.dialogs.ChooseCommitDialog;
import me.sheimi.sgit.listeners.OnBackClickListener;
import me.sheimi.sgit.utils.FsUtils;
import me.sheimi.sgit.utils.RepoUtils;

/**
 * Created by sheimi on 8/5/13.
 */
public class FilesFragment extends BaseFragment {

    private FsUtils mFsUtils;
    private RepoUtils mRepoUtils;

    private String mLocalRepo;

    private Button mCommitNameButton;
    private ImageView mCommitType;
    private ListView mFilesList;
    private FilesListAdapter mFilesListAdapter;

    private File mCurrentDir;
    private File mRootDir;

    private Git mGit;

    private RepoDetailActivity mActivity;

    public FilesFragment(Git git, String localRepo) {
        mGit = git;
        mLocalRepo = localRepo;
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_files, container, false);
        mActivity = (RepoDetailActivity) getActivity();
        mRepoUtils = RepoUtils.getInstance(mActivity);
        mFsUtils = FsUtils.getInstance(getActivity());

        mRootDir = mFsUtils.getRepo(mLocalRepo);

        mCommitNameButton = (Button) v.findViewById(R.id.commitName);
        mCommitType = (ImageView) v.findViewById(R.id.commitType);
        mFilesList = (ListView) v.findViewById(R.id.filesList);

        mFilesListAdapter = new FilesListAdapter(getActivity());
        mFilesList.setAdapter(mFilesListAdapter);


        mFilesList.setOnItemClickListener(new AdapterView.OnItemClickListener
                () {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                File file = mFilesListAdapter.getItem(position);
                if (!file.isDirectory())
                    return;
                setCurrentDir(file);
            }
        });

        mCommitNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseCommitDialog cbd = new ChooseCommitDialog(mGit);
                cbd.show(getFragmentManager(), "choose-branch-dialog");
            }
        });

        String branchName = mRepoUtils.getBranchName(mGit);
        reset(branchName);

        return v;
    }


    public void setCurrentDir(File dir) {
        mCurrentDir = dir;
        if (mFilesListAdapter != null) {
            mFilesListAdapter.setDir(mCurrentDir);
        }
    }

    public void resetCurrentDir() {
        setCurrentDir(mRootDir);
    }

    public void reset(String commitName) {
        int commitType = mRepoUtils.getCommitType(commitName);
        switch (commitType) {
            case RepoUtils.COMMIT_TYPE_HEAD:
                mCommitType.setVisibility(View.VISIBLE);
                mCommitType.setImageResource(R.drawable.ic_branch_w);
                break;
            case RepoUtils.COMMIT_TYPE_TAG:
                mCommitType.setVisibility(View.VISIBLE);
                mCommitType.setImageResource(R.drawable.ic_tag_w);
                break;
            case RepoUtils.COMMIT_TYPE_TEMP:
                mCommitType.setVisibility(View.GONE);
                break;
        }
        String displayName = mRepoUtils.getCommitDisplayName(commitName);
        mCommitNameButton.setText(displayName);
        resetCurrentDir();
    }

    @Override
    public OnBackClickListener getOnBackClickListener() {
        return new OnBackClickListener() {
            @Override
            public boolean onClick() {
                if (mCurrentDir.equals(mRootDir))
                    return false;
                File parent = mCurrentDir.getParentFile();
                setCurrentDir(parent);
                return true;
            }
        };
    }
}