package com.goodproductssoft.minningpool.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goodproductssoft.minningpool.CustomApp;
import com.goodproductssoft.minningpool.MyPreferences;
import com.goodproductssoft.minningpool.OnBroadcastService;
import com.goodproductssoft.minningpool.R;
import com.goodproductssoft.minningpool.models.Miner;
import com.goodproductssoft.minningpool.util.AppLovinCustomEventBanner;
import com.goodproductssoft.minningpool.util.IabHelper;
import com.goodproductssoft.minningpool.util.IabResult;
import com.goodproductssoft.minningpool.util.Inventory;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.mopub.mobileads.dfp.adapters.MoPubAdapter;

import java.util.ArrayList;
import java.util.Random;

import static com.google.android.gms.ads.AdSize.BANNER;
import static com.google.android.gms.ads.AdSize.SMART_BANNER;

//import com.mopub.mobileads.MoPubErrorCode;
//import com.mopub.mobileads.MoPubView;

public class MainActivity extends AppCompatActivity implements FragmentMiner.ProgressDisplay,
        FragmentWorker.ProgressDisplay, FragmentPayouts.ProgressDisplay, FragmentPoolSettings.IProgressDisplay{
    //SharedPreferences pref;
    ArrayList<Miner> miners;
    ImageView btnWorker, btnMiner, btnPayouts, btnSettings, icon_app;
    TextView title_app;
    RelativeLayout progressbar;
    LinearLayout tab_settings, tab_payouts, tab_workers, tab_miner, id_ads_app;

//    private AdView mAdView;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd rewardedVideoAd;
//    com.facebook.ads.AdView adViewFacebook;

    private static final String ADMOB_AD_UNIT_ID_INTERSTITIAL = "ca-app-pub-1827062885697339/8801120573";
    private static final String ADMOB_AD_UNIT_ID_REWARDEDVIDEO = "ca-app-pub-1827062885697339/8995372320";
    public final static int MAX_SHOW_ADS_REMAIN_TIMES = 25;
    public final static int MIN_SHOW_ADS_REMAIN_TIMES = 15;
    public final static boolean IS_SHOW_ADS = true;
    public final static int SHOW_RATE_REMAIN_TIMES = 25;

    Handler UIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UIHandler = new Handler();

        setContentView(R.layout.activity_main);

//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                String hashKey = new String(Base64.encode(md.digest(), 0));
//                Log.i("printHashKey", "printHashKey() Hash Key: " + hashKey);
//            }
//        } catch (Exception e) {
//            Log.e("printHashKey", "printHashKey()", e);
//        }

        final IabHelper mHelper = CustomApp.getInstance().createIaHelper();

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                                   public void onIabSetupFinished(IabResult result) {
                                       if (result.isSuccess()) {
                                           try {
                                               ArrayList<String> items = new ArrayList<>();
                                               items.add(CustomApp.ADS_ITEM_SKU);
                                               Inventory inventory = mHelper.queryInventory(true, items);
                                               MyPreferences myPreferences = MyPreferences.getInstance();
                                               if(inventory.hasPurchase(CustomApp.ADS_ITEM_SKU)){
                                                   myPreferences.setRemoveAds(true);
                                                   hideBannerAds();
                                               } else {
                                                   myPreferences.setRemoveAds(false);
                                               }
                                           }
                                           catch (Exception ex){
                                           }
                                       }
                                   }
                               });




        btnWorker = findViewById(R.id.btn_worker);
        btnMiner = findViewById(R.id.btn_miner);
        btnSettings = findViewById(R.id.btn_pool_settings);
        btnPayouts = findViewById(R.id.btn_payouts);
        progressbar = findViewById(R.id.progressbar);
        tab_settings = findViewById(R.id.tab_settings);
        tab_payouts = findViewById(R.id.tab_payouts);
        tab_workers = findViewById(R.id.tab_workers);
        tab_miner = findViewById(R.id.tab_miner);
//        mAdView = findViewById(R.id.adView);
        id_ads_app = findViewById(R.id.id_ads_app);
        icon_app = findViewById(R.id.icon_app);
        title_app = findViewById(R.id.title_app);

        miners = MyPreferences.getInstance().GetIdMiners();
        Miner minerActive = GetMinerIdActive();

        if(minerActive != null) {
            TabMinerSelected();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            Fragment fragment = new FragmentMiner();
            fragmentTransaction.replace(R.id.fragment_content, fragment);
            fragmentTransaction.commitAllowingStateLoss();
        } else {
            TabSettingsSelected();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            Fragment fragment = new FragmentPoolSettings();
            fragmentTransaction.replace(R.id.fragment_content, fragment);
            fragmentTransaction.commitAllowingStateLoss();
        }

        id_ads_app.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    title_app.setTextColor(getResources().getColor(R.color.color_press_primary));
//                    icon_app.setImageResource(R.drawable.icon_eth_press);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    title_app.setTextColor(getResources().getColor(R.color.colorWhite));
//                    icon_app.setImageResource(R.drawable.icon_eth);

                    try {
                        //Close keyBoard in transition
                        InputMethodManager inputManager = (InputMethodManager) MainActivity.this.getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    catch (Exception ex){}

                    TabAdsAppSelected();
                    FragmentAdsApp fragment = new FragmentAdsApp();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_content, fragment);
                    transaction.commitAllowingStateLoss();

                }
                return true;
            }
        });
        final View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View view) {
                //Fragment fragment = null;

                try {
                    //Close keyBoard in transition
                    InputMethodManager inputManager = (InputMethodManager) MainActivity.this.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                catch (Exception ex){}

                if(view == findViewById(R.id.btn_worker)){
                    TabWorkersSelected();
                    FragmentWorker fragment = new FragmentWorker();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_content, fragment);
                    transaction.commitAllowingStateLoss();
                } else if(view == findViewById(R.id.btn_miner)){
                    TabMinerSelected();
                    FragmentMiner fragment = new FragmentMiner();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_content, fragment);
                    transaction.commitAllowingStateLoss();
                }
                else if(view == findViewById(R.id.btn_payouts)){
                    TabPayoutsSelected();
                    FragmentPayouts fragment = new FragmentPayouts();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_content, fragment);
                    transaction.commitAllowingStateLoss();
                }
                else {
                    TabSettingsSelected();
                    FragmentPoolSettings fragment = new FragmentPoolSettings();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_content, fragment);
                    transaction.commitAllowingStateLoss();
                }
            }
        };

        btnWorker.setOnClickListener(listener);
        btnMiner.setOnClickListener(listener);
        btnSettings.setOnClickListener(listener);
        btnPayouts.setOnClickListener(listener);

        AlarmManager processTimer = (AlarmManager)getSystemService(ALARM_SERVICE);
        int time_for_repeate = 1000 * 120;
        Intent intent = new Intent(MainActivity.this, OnBroadcastService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 1012, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        processTimer.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time_for_repeate, pendingIntent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
//            if (IS_SHOW_ADS && !MyPreferences.getInstance().getRemoveAds()) {
//                if (mAdView.getVisibility() == View.GONE) {
                    setBannerAds();
//                }
//            }
        }
        catch (Exception ex){}
    }

//    private void setMopubBannerAds() {
//        MoPubView moPubView = new MoPubView(this);
//        moPubView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        moPubView.setAdUnitId("98edab2ef5564532a1060d529a23345b"); // Enter your Ad Unit ID from www.mopub.com
////        moPubView.setTesting(true);
//        moPubView.setBannerAdListener(new MoPubView.BannerAdListener() {
//            @Override
//            public void onBannerLoaded(MoPubView banner) {
//
//            }
//
//            @Override
//            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
//
//            }
//
//            @Override
//            public void onBannerClicked(MoPubView banner) {
//
//            }
//
//            @Override
//            public void onBannerExpanded(MoPubView banner) {
//
//            }
//
//            @Override
//            public void onBannerCollapsed(MoPubView banner) {
//
//            }
//        });
//        ((ViewGroup)findViewById(R.id.adView)).removeAllViews();
//        ((ViewGroup)findViewById(R.id.adView)).addView(moPubView);
//        moPubView.loadAd();
//    }

    @Override
    public void onAttachFragment(android.support.v4.app.Fragment fragment) {
        super.onAttachFragment(fragment);
        if(IS_SHOW_ADS) {
//        if(fragment instanceof FragmentMiner
//                || fragment instanceof FragmentPayouts
//                || fragment instanceof FragmentWorker
//                || fragment instanceof FragmentPoolSettings) {
            showAds();
//        }
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof FragmentMiner
                || fragment instanceof FragmentPayouts
                || fragment instanceof FragmentWorker
                || fragment instanceof FragmentPoolSettings) {
            try {
                MyPreferences myPreferences = MyPreferences.getInstance();
                long remainTimes = Math.min(SHOW_RATE_REMAIN_TIMES, myPreferences.getShowRateRemainTimes());
                if (remainTimes > 0) {
                    remainTimes--;
                    myPreferences.setShowRateRemainTimes(remainTimes);
                    if(remainTimes == 0) {
                        long remainAdsTimes = myPreferences.getShowAdsRemainTimes();
                        myPreferences.setShowAdsRemainTimes(Math.max(remainAdsTimes, 6));

                        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                        alertDialog.setTitle("Rate us!");
                        alertDialog.setMessage("Could you please rate us 5 stars? Thank you.");
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Not now ",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes, rate now",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri uri = Uri.parse("market://details?id=" + getPackageName());
                                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                        try {
                                            startActivity(goToMarket);
                                        } catch (Exception e) {
                                            startActivity(new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                                        }
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                }
            } catch (Exception ex) {}

            if (IS_SHOW_ADS) {
                showAds();
            }
        }
    }



    private Miner GetMinerIdActive(){
        if(miners != null && !miners.isEmpty()) {
            for (int i = 0; i < miners.size(); i++) {
                if (miners.get(i).isActive()) {
                    return miners.get(i);
                }
            }
        }
        return null;
    }

    private void TabAdsAppSelected(){
        id_ads_app.setBackgroundResource(R.color.background_selected);
        tab_settings.setBackgroundResource(R.color.background_header);
        tab_payouts.setBackgroundResource(R.color.background_header);
        tab_workers.setBackgroundResource(R.color.background_header);
        tab_miner.setBackgroundResource(R.color.background_header);
    }

    private void TabSettingsSelected(){
        id_ads_app.setBackgroundResource(R.color.background_header);
        tab_settings.setBackgroundResource(R.color.background_selected);
        tab_payouts.setBackgroundResource(R.color.background_header);
        tab_workers.setBackgroundResource(R.color.background_header);
        tab_miner.setBackgroundResource(R.color.background_header);
    }

    private void TabPayoutsSelected(){
        id_ads_app.setBackgroundResource(R.color.background_header);
        tab_settings.setBackgroundResource(R.color.background_header);
        tab_payouts.setBackgroundResource(R.color.background_selected);
        tab_workers.setBackgroundResource(R.color.background_header);
        tab_miner.setBackgroundResource(R.color.background_header);
    }

    private void TabWorkersSelected(){
        id_ads_app.setBackgroundResource(R.color.background_header);
        tab_settings.setBackgroundResource(R.color.background_header);
        tab_payouts.setBackgroundResource(R.color.background_header);
        tab_workers.setBackgroundResource(R.color.background_selected);
        tab_miner.setBackgroundResource(R.color.background_header);
    }

    @Override
    public void TabMinerSelected(){
        id_ads_app.setBackgroundResource(R.color.background_header);
        tab_settings.setBackgroundResource(R.color.background_header);
        tab_payouts.setBackgroundResource(R.color.background_header);
        tab_workers.setBackgroundResource(R.color.background_header);
        tab_miner.setBackgroundResource(R.color.background_selected);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        if(!MyPreferences.getInstance().getRemoveAds()){
            setBannerAds();
//        }
    }

    @Override
    public void showProgress(){
        progressbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress(){
        progressbar.setVisibility(View.GONE);
    }

    public void hideBannerAds() {
//       if(mAdView != null) {
//            mAdView.setVisibility(View.GONE);
//       }
    }

    private void setBannerAds() {
        setBannerAds(SMART_BANNER);
    }


    private void setBannerAds(final AdSize adSize){
        //TODO: AppLovinAds
//        final AppLovinAdView adView = new AppLovinAdView( AppLovinAdSize.BANNER, this );
//
//        ((ViewGroup)findViewById(R.id.adView)).removeAllViews();
//        ((ViewGroup)findViewById(R.id.adView)).addView(adView);
//
//        adView.setLayoutParams( new RelativeLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, com.applovin.sdk.AppLovinSdkUtils.dpToPx( this, AppLovinAdSize.BANNER.getHeight() ) ) );
//        adView.setAdDisplayListener(new AppLovinAdDisplayListener() {
//            @Override
//            public void adDisplayed(AppLovinAd appLovinAd) {
//
//            }
//
//            @Override
//            public void adHidden(AppLovinAd appLovinAd) {
//
//            }
//        });
//        adView.setAdLoadListener(new AppLovinAdLoadListener() {
//            @Override
//            public void adReceived(AppLovinAd appLovinAd) {
//            }
//
//            @Override
//            public void failedToReceiveAd(int i) {
//
//            }
//        });
//
//
//        // Load an ad!
//        adView.loadNextAd();

//        // Load an Interstitial Ad
//        AppLovinSdk.getInstance( this ).getAdService().loadNextAd( AppLovinAdSize.INTERSTITIAL, new AppLovinAdLoadListener()
//        {
//            @Override
//            public void adReceived(AppLovinAd ad)
//            {
//                AppLovinInterstitialAdDialog interstitialAd = AppLovinInterstitialAd.create( AppLovinSdk.getInstance( MainActivity.this ), MainActivity.this );
//
////                interstitialAd.setAdDisplayListener( ... );
////                interstitialAd.setAdClickListener( ... );
////                interstitialAd.setAdVideoPlaybackListener( ... );
//
//                interstitialAd.showAndRender( ad );
//            }
//
//            @Override
//            public void failedToReceiveAd(int errorCode)
//            {
//                // Look at AppLovinErrorCodes.java for list of error codes.
//            }
//        } );

        mAdView = new AdView(this);
        mAdView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mAdView.setAdSize(adSize);
        mAdView.setAdUnitId("ca-app-pub-1827062885697339/3931937276");
        mAdView.setVisibility(View.GONE);

        //TODO: bundle for AppLovin banners
        Bundle bundleAppLovin = new Bundle();
        bundleAppLovin.putString( "zone_id", "9730ce82a7965f77" );

        //TODO: bundle for Mopub
        Bundle bundleMopub = new MoPubAdapter.BundleBuilder()
                .build();

        //TODO: bundle for Ads facebook
//        Bundle extras = new FacebookAdapter.FacebookExtrasBundleBuilder()
//                .setNativeAdChoicesIconExpandable(false)
//                .build();

        AdRequest adRequest = new AdRequest.Builder()
//                    .addNetworkExtrasBundle(FacebookAdapter.class, extras)
                    .addCustomEventExtrasBundle(AppLovinCustomEventBanner.class, bundleAppLovin )
                    .addNetworkExtrasBundle(MoPubAdapter.class, bundleMopub)
                    .build();


        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
//                if (!MyPreferences.getInstance().getRemoveAds()) {
                    mAdView.setVisibility(View.VISIBLE);
//                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                mAdView.setVisibility(View.GONE);
                if(errorCode == 3){
                    if(adSize == SMART_BANNER){
                        setBannerAds(BANNER);
                    } else{
                        showAds();
                    }
                }
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });

        //TODO: test mopub
//        mAdView = new MoPubView(this);
//        mAdView.setAdUnitId("98edab2ef5564532a1060d529a23345b"); // Enter your Ad Unit ID from www.mopub.com
//        mAdView.loadAd();
//        mAdView.setBannerAdListener(new MoPubView.BannerAdListener() {
//            @Override
//            public void onBannerLoaded(MoPubView banner) {
//
//            }
//
//            @Override
//            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
//
//            }
//
//            @Override
//            public void onBannerClicked(MoPubView banner) {
//
//            }
//
//            @Override
//            public void onBannerExpanded(MoPubView banner) {
//
//            }
//
//            @Override
//            public void onBannerCollapsed(MoPubView banner) {
//
//            }
//        });
        ((ViewGroup)findViewById(R.id.adView)).removeAllViews();
        ((ViewGroup)findViewById(R.id.adView)).addView(mAdView);

        mAdView.loadAd(adRequest);
    }



    @Override
    protected void onDestroy() {
//        if (adViewFacebook != null) {
//            adViewFacebook.destroy();
//        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager != null) {
            Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_content);
            if (fragment instanceof FragmentAdsApp) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private boolean lockedAds = false;
    private void showAds() {
        try {
            MyPreferences myPreferences = MyPreferences.getInstance();
            if (!myPreferences.getRemoveAds()) {
                long remainTimes = myPreferences.getShowAdsRemainTimes();
                if (remainTimes > 0) {
                    myPreferences.setShowAdsRemainTimes(remainTimes - 1);
                } else if (!lockedAds && UIHandler != null) {
                    long remainAdsTimes = myPreferences.getShowRateRemainTimes();
                    if (remainAdsTimes > 0) {
                        myPreferences.setShowRateRemainTimes(Math.max(remainAdsTimes, 6));
                    }

                    lockedAds = true;
                    UIHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getIntersitialAds();
                                UIHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        lockedAds = false;
                                    }
                                }, 5000);
                            } catch (Exception ex) {
                                lockedAds = false;
                            }
                        }
                    }, 4000);
                }
            }
        }
        catch (Exception ex){
            lockedAds = false;
        }
    }

    /**
     * Add Intersitial Ads
     * */
    public void getIntersitialAds(){
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(ADMOB_AD_UNIT_ID_INTERSTITIAL);

        // Set an AdListener.
        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdLoaded() {
                if (mInterstitialAd.isLoaded()) {
                    MyPreferences myPreferences = MyPreferences.getInstance();
                    myPreferences.setShowAdsRemainTimes(new Random().nextInt(MAX_SHOW_ADS_REMAIN_TIMES - MIN_SHOW_ADS_REMAIN_TIMES + 1) + MIN_SHOW_ADS_REMAIN_TIMES );
                    mInterstitialAd.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                if (i == 3) {
                    getRewardedVideoAds();
                }
            }

            @Override
            public void onAdClosed() {
            }
        });

        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    /**
     * ADd RewardedVideo Ads
     * */
    private void getRewardedVideoAds() {
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener()
        {
            @Override
            public void onRewardedVideoAdLeftApplication() {
            }

            @Override
            public void onRewardedVideoAdClosed() {
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int errorCode) {
            }

            @Override
            public void onRewardedVideoAdLoaded() {
                showRewardedVideo();
            }

            @Override
            public void onRewardedVideoAdOpened() {
            }

            @Override
            public void onRewarded(RewardItem reward) {
            }

            @Override
            public void onRewardedVideoStarted() {
            }

            @Override
            public void onRewardedVideoCompleted() {
            }
        });

        loadRewardedVideoAd();
        showRewardedVideo();
    }

    private void loadRewardedVideoAd() {
        if (!rewardedVideoAd.isLoaded()) {
            rewardedVideoAd.loadAd(ADMOB_AD_UNIT_ID_REWARDEDVIDEO, new AdRequest.Builder().build());
        }
    }

    private void showRewardedVideo() {
        if (rewardedVideoAd.isLoaded()) {
            MyPreferences myPreferences = MyPreferences.getInstance();
            myPreferences.setShowAdsRemainTimes(new Random().nextInt(MAX_SHOW_ADS_REMAIN_TIMES - MIN_SHOW_ADS_REMAIN_TIMES + 1) + MIN_SHOW_ADS_REMAIN_TIMES );
            rewardedVideoAd.show();
        }
    }



//    private void setBannerAdsFacebook(){
//        adViewFacebook = new com.facebook.ads.AdView(this, "1885266801771264_1885266888437922", com.facebook.ads.AdSize.BANNER_HEIGHT_50);
//        ((ViewGroup)findViewById(R.id.adView)).removeAllViews();
//        ((ViewGroup)findViewById(R.id.adView)).addView(adViewFacebook);
//        adViewFacebook.setAdListener(new AbstractAdListener() {
//            @Override
//            public void onError(Ad ad, AdError adError) {
//                super.onError(ad, adError);
//                // Ad error callback
//                Toast.makeText(MainActivity.this, "Error: " + adError.getErrorMessage(), Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onAdLoaded(Ad ad) {
//                super.onAdLoaded(ad);
//            }
//
//            @Override
//            public void onAdClicked(Ad ad) {
//                super.onAdClicked(ad);
//            }
//
//            @Override
//            public void onLoggingImpression(Ad ad) {
//                super.onLoggingImpression(ad);
//            }
//        });
////        AdSettings.addTestDevice("HASHED ID");
//        adViewFacebook.loadAd();
//    }
}
