package de.haw_landshut.hawmobile.mail;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import de.haw_landshut.hawmobile.MainActivity;
import de.haw_landshut.hawmobile.R;
import de.haw_landshut.hawmobile.base.EMail;
import de.haw_landshut.hawmobile.base.EMailDao;

import javax.mail.MessagingException;
import javax.mail.Store;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MailOverview.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MailOverview#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MailOverview extends Fragment {

    private OnFragmentInteractionListener mListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mFloatingActionButton;
    private SharedPreferences preferences;
    private RecyclerView mRecyclerView;
    private EMailDao eMailDao;

    private Store store;

    public MailOverview() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MailOverview.
     */
    public static MailOverview newInstance() {
        return new MailOverview();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

        eMailDao = MainActivity.getHawDatabase().eMailDao();

        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        if(!preferences.getBoolean("mailsFetched", false))
            new Mail2BaseTask().execute();

    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().setTitle(R.string.INBOX);
        menu.clear();
        inflater.inflate(R.menu.mailactionbar_default, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("onOptionsItemSelected", item.getTitle().toString());
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mail_overview, container, false);

        mRecyclerView = view.findViewById(R.id.mailsRecycleView);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), RecyclerView.VERTICAL));

        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new UpdateBase().execute();
            }
        });

        mFloatingActionButton = view.findViewById(R.id.createMail);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //Fügt E-Mails in die Liste ein
        new Base2MailEntryAdapter().execute("INBOX");

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    /**
     * Fetches all Mails and writes them into the Database
     */
    private class Mail2BaseTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPostExecute(Void aVoid) {
            preferences.edit().putBoolean("mailsFetched", true).apply();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final EMailDao eMailDao = MainActivity.getHawDatabase().eMailDao();

            try {
                Protocol.login();
                Protocol.loadAllMessagesAndFolders(eMailDao);
                Protocol.logout();
            } catch (MessagingException e){
                Toast.makeText(getContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
            }

            return null;
        }
    }

    /**
     * Obtaines all Emails from Database and creates an Adapter where the E-Mails will be inserted into
     */
    private class Base2MailEntryAdapter extends AsyncTask<String, Void, MailEntryAdapter>{
        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(MailEntryAdapter mea) {
            mRecyclerView.setAdapter(mea);
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected MailEntryAdapter doInBackground(String... name) {

            final List<EMail> mailList = eMailDao.getAllEmailsFromFolder(name[0]);

            return new MailEntryAdapter(mailList);
        }
    }

    /**
     * Updates The Database with new E-Mails from the Mail-Server
     */
    private class UpdateBase extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPostExecute(Void aVoid) {
            new Base2MailEntryAdapter().execute("INBOX");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Protocol.login();
                Protocol.updateAllFolders(eMailDao);
                Protocol.logout();
            } catch (MessagingException e){
                Toast.makeText(getContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
            }

            return null;
        }
    }

}
