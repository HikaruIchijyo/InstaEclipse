package ps.reso.instaeclipse.utils.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.mods.ui.InstagramUI;
import ps.reso.instaeclipse.utils.core.SettingsManager;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;
import ps.reso.instaeclipse.utils.ghost.GhostModeUtils;

public class DialogUtils {

    private static AlertDialog currentDialog;

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void showEclipseOptionsDialog(Context context) {
        SettingsManager.init(context);
        Context themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Material_Dialog_Alert);

        LinearLayout mainLayout = buildMainMenuLayout(themedContext);
        ScrollView scrollView = new ScrollView(themedContext);
        scrollView.addView(mainLayout);

        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        currentDialog = new AlertDialog.Builder(themedContext)
                .setView(scrollView)
                .setTitle(null)
                .setCancelable(true)
                .create();

        Objects.requireNonNull(currentDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        currentDialog.show();
    }


    @SuppressLint("SetTextI18n")
    private static LinearLayout buildMainMenuLayout(Context context) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 20);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#262626"));
        background.setCornerRadius(32);
        mainLayout.setBackground(background);

        // Title
        TextView title = new TextView(context);
        title.setText("InstaEclipse 🌘");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 20);
        mainLayout.addView(title);

        mainLayout.addView(createDivider(context));

        // Now building menu manually
        // 0 - Developer Mode => DIRECT SWITCH
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch devSwitch = createSwitch(context, "🎛 Developer Mode", FeatureFlags.isDevEnabled);
        devSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FeatureFlags.isDevEnabled = isChecked;
            SettingsManager.saveAllFlags();
        });
        mainLayout.addView(devSwitch);

        mainLayout.addView(createDivider(context));

        // 1 - Ghost Mode Settings => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "👻 Ghost Mode Settings", () -> showGhostOptions(context)));

        // 2 - Ad/Analytics Block => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "🛡 Ad/Analytics Block", () -> showAdOptions(context)));

        // 3 - Distraction-Free Instagram => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "🧘 Distraction-Free Instagram", () -> showDistractionOptions(context)));

        // 4 - Misc Features => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "⚙ Misc Features", () -> showMiscOptions(context)));

        // 5 - About => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "ℹ️ About", () -> showAboutDialog(context)));

        // 6 - Restart Instagram => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "🔁 Restart Instagram", () -> showRestartSection(context)));

        mainLayout.addView(createDivider(context));

        // Footer Credit
        TextView footer = new TextView(context);
        footer.setText("@reso7200");
        footer.setTextColor(Color.GRAY);
        footer.setTextSize(14);
        footer.setPadding(0, 30, 0, 10);
        footer.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.addView(footer);

        // Embedded Close Button
        TextView closeButton = new TextView(context);
        closeButton.setText("❌ Close");
        closeButton.setTextColor(Color.WHITE);
        closeButton.setTextSize(16);
        closeButton.setPadding(20, 30, 20, 30);
        closeButton.setGravity(Gravity.CENTER);
        closeButton.setBackgroundResource(android.R.drawable.list_selector_background);
        closeButton.setOnClickListener(v -> {
            if (currentDialog != null) currentDialog.dismiss();
        });

        mainLayout.addView(createDivider(context)); // Divider above close button
        mainLayout.addView(closeButton);

        SettingsManager.saveAllFlags();

        Activity activity = InstagramUI.getCurrentActivity();
        if (activity != null) {
            InstagramUI.addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
        }

        return mainLayout;
    }


    private static void showGhostQuickToggleOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);

        // Create switches for customizing what gets toggled
        Switch[] toggleSwitches = new Switch[]{
                createSwitch(context, "Include Hide Seen", FeatureFlags.quickToggleSeen),
                createSwitch(context, "Include Hide Typing", FeatureFlags.quickToggleTyping),
                createSwitch(context, "Include Disable Screenshot Detection", FeatureFlags.quickToggleScreenshot),
                createSwitch(context, "Include Hide View Once", FeatureFlags.quickToggleViewOnce),
                createSwitch(context, "Include Hide Story Seen", FeatureFlags.quickToggleStory),
                createSwitch(context, "Include Hide Live Seen", FeatureFlags.quickToggleLive)
        };

        // Create Enable/Disable All switch
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch enableAllSwitch = createSwitch(context, "Enable/Disable All", areAllEnabled(toggleSwitches));

        // Master listener
        enableAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Switch s : toggleSwitches) {
                s.setChecked(isChecked);
            }
        });

        // Individual switch listeners (update master switch automatically)
        for (int i = 0; i < toggleSwitches.length; i++) {
            final int index = i;
            toggleSwitches[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                enableAllSwitch.setOnCheckedChangeListener(null);
                enableAllSwitch.setChecked(areAllEnabled(toggleSwitches));
                enableAllSwitch.setOnCheckedChangeListener((buttonView2, isChecked2) -> {
                    for (Switch s2 : toggleSwitches) {
                        s2.setChecked(isChecked2);
                    }
                });

                // Update corresponding FeatureFlag instantly
                switch (index) {
                    case 0:
                        FeatureFlags.quickToggleSeen = isChecked;
                        break;
                    case 1:
                        FeatureFlags.quickToggleTyping = isChecked;
                        break;
                    case 2:
                        FeatureFlags.quickToggleScreenshot = isChecked;
                        break;
                    case 3:
                        FeatureFlags.quickToggleViewOnce = isChecked;
                        break;
                    case 4:
                        FeatureFlags.quickToggleStory = isChecked;
                        break;
                    case 5:
                        FeatureFlags.quickToggleLive = isChecked;
                        break;
                }

                // Save immediately
                SettingsManager.saveAllFlags();

                // Update ghost emoji immediately
                Activity activity = InstagramUI.getCurrentActivity();
                if (activity != null) {
                    InstagramUI.addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
                }
            });
        }


        // Add views to layout
        layout.addView(createDivider(context)); // Divider above
        layout.addView(createEnableAllSwitch(context, enableAllSwitch)); // Styled enable all switch
        layout.addView(createDivider(context)); // Divider below

        for (Switch s : toggleSwitches) {
            layout.addView(s);
        }

        // Show dialog
        showSectionDialog(context, "Customize Quick Toggle 🛠️", layout, () -> {
        });

    }


    private static View createDivider(Context context) {
        View divider = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        params.setMargins(0, 20, 0, 20);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(Color.DKGRAY);
        return divider;
    }

    private static void restartInstagram(Context context) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.instagram.android");
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Runtime.getRuntime().exit(0);
            } else {
                Toast.makeText(context, "Instagram not found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            XposedBridge.log("InstaEclipse: Restart failed - " + e.getMessage());
            Toast.makeText(context, "Restart failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ==== SECTIONS ====

    private static void showGhostOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);

        Switch[] switches = new Switch[]{
                createSwitch(context, "Hide Seen", FeatureFlags.isGhostSeen),
                createSwitch(context, "Hide Typing", FeatureFlags.isGhostTyping),
                createSwitch(context, "Disable Screenshot Detection", FeatureFlags.isGhostScreenshot),
                createSwitch(context, "Hide View Once", FeatureFlags.isGhostViewOnce),
                createSwitch(context, "Hide Story Seen", FeatureFlags.isGhostStory),
                createSwitch(context, "Hide Live Seen", FeatureFlags.isGhostLive)
        };

        layout.addView(createClickableSection(context, "🛠 Customize Quick Toggle", () -> showGhostQuickToggleOptions(context)));

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch enableAllSwitch = createSwitch(context, "Enable/Disable All", areAllEnabled(switches));

        enableAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Switch s : switches) {
                s.setChecked(isChecked);
            }
        });

        for (int i = 0; i < switches.length; i++) {
            final int index = i;
            switches[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                enableAllSwitch.setOnCheckedChangeListener(null);
                enableAllSwitch.setChecked(areAllEnabled(switches));
                enableAllSwitch.setOnCheckedChangeListener((buttonView2, isChecked2) -> {
                    for (Switch s2 : switches) {
                        s2.setChecked(isChecked2);
                    }
                });

                // Set FeatureFlag immediately
                switch (index) {
                    case 0:
                        FeatureFlags.isGhostSeen = isChecked;
                        break;
                    case 1:
                        FeatureFlags.isGhostTyping = isChecked;
                        break;
                    case 2:
                        FeatureFlags.isGhostScreenshot = isChecked;
                        break;
                    case 3:
                        FeatureFlags.isGhostViewOnce = isChecked;
                        break;
                    case 4:
                        FeatureFlags.isGhostStory = isChecked;
                        break;
                    case 5:
                        FeatureFlags.isGhostLive = isChecked;
                        break;
                }

                // Save immediately
                SettingsManager.saveAllFlags();

                // Update ghost emoji immediately
                Activity activity = InstagramUI.getCurrentActivity();
                if (activity != null) {
                    InstagramUI.addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
                }
            });
        }

        layout.addView(createDivider(context));
        layout.addView(createEnableAllSwitch(context, enableAllSwitch));
        layout.addView(createDivider(context));

        for (Switch s : switches) {
            layout.addView(s);
        }

        showSectionDialog(context, "Ghost Mode 👻", layout, () -> {
            // No need to set FeatureFlags here anymore because handled instantly
        });
    }


    private static void showAdOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);

        // Create switches
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch adBlock = createSwitch(context, "Block Ads", FeatureFlags.isAdBlockEnabled);

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch analytics = createSwitch(context, "Block Analytics", FeatureFlags.isAnalyticsBlocked);

        Switch[] switches = new Switch[]{adBlock, analytics};

        // Create Enable/Disable All switch
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch enableAllSwitch = createSwitch(context, "Enable/Disable All", areAllEnabled(switches));

        // Master listener
        enableAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Switch s : switches) {
                s.setChecked(isChecked);
            }
        });

        // Individual switch listeners
        for (int i = 0; i < switches.length; i++) {
            final int index = i;
            switches[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                enableAllSwitch.setOnCheckedChangeListener(null);
                enableAllSwitch.setChecked(areAllEnabled(switches));
                enableAllSwitch.setOnCheckedChangeListener((buttonView2, isChecked2) -> {
                    for (Switch s2 : switches) {
                        s2.setChecked(isChecked2);
                    }
                });

                // Update FeatureFlag immediately
                if (index == 0) FeatureFlags.isAdBlockEnabled = isChecked;
                if (index == 1) FeatureFlags.isAnalyticsBlocked = isChecked;

                // Save immediately
                SettingsManager.saveAllFlags();
            });
        }


        // Add views
        layout.addView(createDivider(context));
        layout.addView(createEnableAllSwitch(context, enableAllSwitch));
        layout.addView(createDivider(context));

        for (Switch s : switches) {
            layout.addView(s);
        }

        // Show the dialog
        showSectionDialog(context, "Ad/Analytics Block 🛡️", layout, () -> {
        });
    }


    private static void showDistractionOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);

        // Create all child switches
        Switch[] switches = new Switch[]{
                createSwitch(context, "Disable Stories", FeatureFlags.disableStories),
                createSwitch(context, "Disable Feed", FeatureFlags.disableFeed),
                createSwitch(context, "Disable Reels", FeatureFlags.disableReels),
                createSwitch(context, "Disable Explore", FeatureFlags.disableExplore),
                createSwitch(context, "Disable Comments", FeatureFlags.disableComments)
        };

        // Create Enable/Disable All switch
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch enableAllSwitch = createSwitch(context, "Enable/Disable All", areAllEnabled(switches));

        // Master listener
        enableAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Switch s : switches) {
                s.setChecked(isChecked);
            }
        });

        // Individual child listeners
        for (Switch s : switches) {
            s.setOnCheckedChangeListener((buttonView, isChecked) -> {
                enableAllSwitch.setOnCheckedChangeListener(null);
                enableAllSwitch.setChecked(areAllEnabled(switches));
                enableAllSwitch.setOnCheckedChangeListener((buttonView2, isChecked2) -> {
                    for (Switch s2 : switches) {
                        s2.setChecked(isChecked2);
                    }
                });
                SettingsManager.saveAllFlags();
            });
        }

        // Add to layout
        layout.addView(createDivider(context));
        layout.addView(createEnableAllSwitch(context, enableAllSwitch));
        layout.addView(createDivider(context));

        for (Switch s : switches) {
            layout.addView(s);
        }

        // Show the dialog
        showSectionDialog(context, "Distraction-Free Instagram 🧘", layout, () -> {
            FeatureFlags.disableStories = switches[0].isChecked();
            FeatureFlags.disableFeed = switches[1].isChecked();
            FeatureFlags.disableReels = switches[2].isChecked();
            FeatureFlags.disableExplore = switches[3].isChecked();
            FeatureFlags.disableComments = switches[4].isChecked();
        });

        SettingsManager.saveAllFlags();
    }


    private static void showMiscOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);

        // Create all child switches
        Switch[] switches = new Switch[]{
                createSwitch(context, "Disable Story Auto-Swipe", FeatureFlags.disableStoryFlipping),
                createSwitch(context, "Disable Video Autoplay", FeatureFlags.disableVideoAutoPlay),
                createSwitch(context, "Show Follower Toast", FeatureFlags.showFollowerToast),
                createSwitch(context, "Show Feature Toasts", FeatureFlags.showFeatureToasts)
        };

        // Create Enable/Disable All switch
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch enableAllSwitch = createSwitch(context, "Enable/Disable All", areAllEnabled(switches));

        // Master listener
        enableAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Switch s : switches) {
                s.setChecked(isChecked);
            }
        });

        // Individual child listeners
        for (int i = 0; i < switches.length; i++) {
            final int index = i;
            switches[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                enableAllSwitch.setOnCheckedChangeListener(null);
                enableAllSwitch.setChecked(areAllEnabled(switches));
                enableAllSwitch.setOnCheckedChangeListener((buttonView2, isChecked2) -> {
                    for (Switch s2 : switches) {
                        s2.setChecked(isChecked2);
                    }
                });

                // Update corresponding FeatureFlag immediately
                switch (index) {
                    case 0:
                        FeatureFlags.disableStoryFlipping = isChecked;
                        break;
                    case 1:
                        FeatureFlags.disableVideoAutoPlay = isChecked;
                        break;
                    case 2:
                        FeatureFlags.showFollowerToast = isChecked;
                        break;
                    case 3:
                        FeatureFlags.showFeatureToasts = isChecked;
                        break;
                }

                // Save immediately
                SettingsManager.saveAllFlags();
            });
        }


        // Add views to layout
        layout.addView(createDivider(context));
        layout.addView(createEnableAllSwitch(context, enableAllSwitch));
        layout.addView(createDivider(context));

        for (Switch s : switches) {
            layout.addView(s);
        }

        // Show the dialog
        showSectionDialog(context, "Miscellaneous ⚙️", layout, () -> {
        });
    }


    @SuppressLint("SetTextI18n")
    private static void showAboutDialog(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(context);
        title.setText("InstaEclipse 🌘");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20f);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);

        TextView creator = new TextView(context);
        creator.setText("Created by @reso7200");
        creator.setTextColor(Color.LTGRAY);
        creator.setTextSize(16f);
        creator.setGravity(Gravity.CENTER);
        creator.setPadding(0, 0, 0, 30);

        Button githubButton = new Button(context);
        githubButton.setText("🌐 GitHub Repo");
        githubButton.setTextColor(Color.WHITE);
        githubButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3F51B5")));
        githubButton.setPadding(40, 20, 40, 20);

        LinearLayout.LayoutParams githubParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        githubParams.gravity = Gravity.CENTER_HORIZONTAL;
        githubButton.setLayoutParams(githubParams);


        githubButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://github.com/ReSo7200/InstaEclipse"));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        });

        layout.addView(title);
        layout.addView(creator);
        layout.addView(githubButton);

        showSectionDialog(context, "About", layout, () -> {
        });
    }

    @SuppressLint("SetTextI18n")
    private static void showRestartSection(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 40);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView message = new TextView(context);
        message.setText("Restart Instagram to apply changes?");
        message.setTextColor(Color.WHITE);
        message.setTextSize(18f);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, 0, 0, 30);

        Button restartButton = new Button(context);
        restartButton.setText("🔁 Restart Now");
        restartButton.setTextColor(Color.WHITE);
        restartButton.setPadding(40, 20, 40, 20);

        restartButton.setOnClickListener(v -> restartInstagram(context));

        layout.addView(message);
        layout.addView(restartButton);

        showSectionDialog(context, "Restart Instagram", layout, () -> {
        });
    }

package ps.reso.instaeclipse.utils.dialog;

import android.annotation.SuppressLint; import android.app.Activity; import android.app.AlertDialog; import android.content.Context; import android.content.Intent; import android.content.res.ColorStateList; import android.graphics.Color; import android.graphics.drawable.ColorDrawable; import android.graphics.drawable.GradientDrawable; import android.view.ContextThemeWrapper; import android.view.Gravity; import android.view.View; import android.widget.Button; import android.widget.LinearLayout; import android.widget.ScrollView; import android.widget.Switch; import android.widget.TextView; import android.widget.Toast;

import java.util.Objects;

import de.robv.android.xposed.XposedBridge; import ps.reso.instaeclipse.mods.ui.InstagramUI; import ps.reso.instaeclipse.utils.core.SettingsManager; import ps.reso.instaeclipse.utils.feature.FeatureFlags; import ps.reso.instaeclipse.utils.ghost.GhostModeUtils;

public class DialogUtils {

private static AlertDialog currentDialog;

@SuppressLint("UseCompatLoadingForDrawables")
public static void showEclipseOptionsDialog(Context context) {
    SettingsManager.init(context);
    Context themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Material_Dialog_Alert);

    LinearLayout mainLayout = buildMainMenuLayout(themedContext);
    ScrollView scrollView = new ScrollView(themedContext);
    scrollView.setPadding(24, 24, 24, 24);
    scrollView.addView(mainLayout);

    if (currentDialog != null && currentDialog.isShowing()) {
        currentDialog.dismiss();
    }

    currentDialog = new AlertDialog.Builder(themedContext)
            .setView(scrollView)
            .setTitle(null)
            .setCancelable(true)
            .create();

    Objects.requireNonNull(currentDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    currentDialog.show();
}

@SuppressLint("SetTextI18n")
private static LinearLayout buildMainMenuLayout(Context context) {
    LinearLayout mainLayout = new LinearLayout(context);
    mainLayout.setOrientation(LinearLayout.VERTICAL);
    mainLayout.setPadding(40, 40, 40, 40);

    GradientDrawable background = new GradientDrawable();
    background.setColor(Color.parseColor("#262626"));
    background.setCornerRadius(48);
    mainLayout.setBackground(background);

    TextView title = new TextView(context);
    title.setText("InstaEclipse 🌘");
    title.setTextColor(Color.WHITE);
    title.setTextSize(22);
    title.setGravity(Gravity.CENTER);
    title.setPadding(0, 20, 0, 20);
    mainLayout.addView(title);

    mainLayout.addView(createDivider(context));

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch devSwitch = createSwitch(context, "🎛 Developer Mode", FeatureFlags.isDevEnabled);
    devSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
        FeatureFlags.isDevEnabled = isChecked;
        SettingsManager.saveAllFlags();
    });
    mainLayout.addView(devSwitch);

    mainLayout.addView(createDivider(context));

    mainLayout.addView(createClickableSection(context, "👻 Ghost Mode Settings", () -> showGhostOptions(context)));
    mainLayout.addView(createClickableSection(context, "🛡 Ad/Analytics Block", () -> showAdOptions(context)));
    mainLayout.addView(createClickableSection(context, "🧘 Distraction-Free Instagram", () -> showDistractionOptions(context)));
    mainLayout.addView(createClickableSection(context, "⚙ Misc Features", () -> showMiscOptions(context)));
    mainLayout.addView(createClickableSection(context, "ℹ️ About", () -> showAboutDialog(context)));
    mainLayout.addView(createClickableSection(context, "🔁 Restart Instagram", () -> showRestartSection(context)));

    mainLayout.addView(createDivider(context));

    TextView footer = new TextView(context);
    footer.setText("@reso7200");
    footer.setTextColor(Color.GRAY);
    footer.setTextSize(14);
    footer.setPadding(0, 30, 0, 10);
    footer.setGravity(Gravity.CENTER_HORIZONTAL);
    mainLayout.addView(footer);

    TextView closeButton = new TextView(context);
    closeButton.setText("❌ Close");
    closeButton.setTextColor(Color.WHITE);
    closeButton.setTextSize(16);
    closeButton.setPadding(20, 30, 20, 30);
    closeButton.setGravity(Gravity.CENTER);
    closeButton.setBackgroundResource(android.R.drawable.list_selector_background);
    closeButton.setOnClickListener(v -> {
        if (currentDialog != null) currentDialog.dismiss();
    });

    mainLayout.addView(createDivider(context));
    mainLayout.addView(closeButton);

    SettingsManager.saveAllFlags();

    Activity activity = InstagramUI.getCurrentActivity();
    if (activity != null) {
        InstagramUI.addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
    }

    return mainLayout;
}

private static View createDivider(Context context) {
    View divider = new View(context);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2);
    params.setMargins(0, 20, 0, 20);
    divider.setLayoutParams(params);
    divider.setBackgroundColor(Color.DKGRAY);
    return divider;
}

private static Switch createSwitch(Context context, String label, boolean isChecked) {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch toggle = new Switch(context);
    toggle.setText(label);
    toggle.setTextColor(Color.WHITE);
    toggle.setTextSize(16);
    toggle.setPadding(20, 20, 20, 20);
    toggle.setChecked(isChecked);
    return toggle;
}

private static LinearLayout createSwitchLayout(Context context) {
    LinearLayout layout = new LinearLayout(context);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(40, 40, 40, 40);

    GradientDrawable background = new GradientDrawable();
    background.setColor(Color.parseColor("#262626"));
    background.setCornerRadius(48);
    layout.setBackground(background);

    return layout;
}

private static View createClickableSection(Context context, String label, Runnable onClick) {
    TextView section = new TextView(context);
    section.setText(label);
    section.setTextColor(Color.WHITE);
    section.setTextSize(16);
    section.setPadding(20, 30, 20, 30);
    section.setGravity(Gravity.START);
    section.setBackgroundResource(android.R.drawable.list_selector_background);
    section.setOnClickListener(v -> onClick.run());
    return section;
}

private static View createEnableAllSwitch(Context context, Switch enableAllSwitch) {
    enableAllSwitch.setTextColor(Color.YELLOW);
    enableAllSwitch.setTextSize(17);
    enableAllSwitch.setPadding(20, 30, 20, 30);
    return enableAllSwitch;
}

private static boolean areAllEnabled(Switch[] switches) {
    for (Switch s : switches) {
        if (!s.isChecked()) return false;
    }
    return true;
}

private static void showSectionDialog(Context context, String title, View layout, Runnable onClose) {
    AlertDialog sectionDialog = new AlertDialog.Builder(context)
            .setTitle(title)
            .setView(layout)
            .setCancelable(true)
            .setOnDismissListener(dialog -> onClose.run())
            .create();

    Objects.requireNonNull(sectionDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    sectionDialog.show();
}

private static void showAboutDialog(Context context) {
    // Implementation placeholder
}

private static void showRestartSection(Context context) {
    // Implementation placeholder
}

private static void showGhostOptions(Context context) {
    // Implementation placeholder
}

private static void showAdOptions(Context context) {
    // Implementation placeholder
}

private static void showDistractionOptions(Context context) {
    // Implementation placeholder
}

private static void showMiscOptions(Context context) {
    // Implementation placeholder
}

}

