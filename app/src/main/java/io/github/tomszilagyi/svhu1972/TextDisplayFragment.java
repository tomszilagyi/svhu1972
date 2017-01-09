package io.github.tomszilagyi.svhu1972;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class TextDisplayFragment extends Fragment {

    static final String EXTRA_ASSET = "io.github.tomszilagyi.svhu1972.TextDisplayFragment.EXTRA_ASSET";

    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_text_display, container, false);
        mWebView = (WebView) view.findViewById(R.id.webview_display);
        String asset = getActivity().getIntent().getStringExtra(EXTRA_ASSET);
        String str;
        try {
            str = readAssetIntoString(asset);
        } catch (IOException e) {
            str = "Could not read asset file "+asset+": "+e.toString();
        }
        mWebView.loadData(str, "text/html; charset=utf-8", "utf-8");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    String readAssetIntoString(String asset) throws IOException {
        AssetManager assetmgr = getActivity().getApplicationContext().getAssets();
        InputStream is = assetmgr.open(asset, AssetManager.ACCESS_STREAMING);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();
        while (line != null) {
            sb.append(line).append("\n");
            line = buf.readLine();
        }
        return sb.toString();
    }
}
