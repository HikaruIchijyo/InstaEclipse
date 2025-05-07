@SuppressLint("SetTextI18n")
    private static LinearLayout buildMainMenuLayout(Context context) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(30, 30, 30, 30);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#0F1216")); // Dark background matching the screenshot
        background.setCornerRadius(24);
        mainLayout.setBackground(background);

        // Title
        TextView title = new TextView(context);
        title.setText("InstaEclipse 🌙");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 0);
        mainLayout.addView(title);

        // Subtitle
        TextView subtitle = new TextView(context);
        subtitle.setText("Power tools for Instagram");
        subtitle.setTextColor(Color.parseColor("#9E9E9E")); // Light gray color for subtitle
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 5, 0, 30);
        mainLayout.addView(subtitle);

        mainLayout.addView(createDivider(context));

        // Menu Items
        
        // 1. Developer Options with code icon
        mainLayout.addView(createSettingsItem(context, "< / >", "Developer Options", () -> {
            // Direct switch for developer mode
            FeatureFlags.isDevEnabled = !FeatureFlags.isDevEnabled;
            SettingsManager.saveAllFlags();
            showEclipseOptionsDialog(context); // Refresh to show updated state
        }));

        // 2. Ghost Mode Settings
        mainLayout.addView(createSettingsItem(context, "👻", "Ghost Mode Settings", () -> showGhostOptions(context)));

        // 3. Ad/Analytics Block
        mainLayout.addView(createSettingsItem(context, "🛡️", "Ad/Analytics Block", () -> showAdOptions(context)));

        // 4. Distraction-Free Instagram
        mainLayout.addView(createSettingsItem(context, "🧘", "Distraction-Free", () -> showDistractionOptions(context)));

        // 5. Misc Features
        mainLayout.addView(createSettingsItem(context, "✖️", "Misc Features", () -> showMiscOptions(context)));

        // 6. About
        mainLayout.addView(createSettingsItem(context, "ℹ️", "About", () -> showAboutDialog(context)));

        // Bottom buttons container - Restart and Close
        LinearLayout buttonContainer = new LinearLayout(context);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setPadding(20, 30, 20, 10);
        LinearLayout.LayoutParams buttonContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonContainerParams.setMargins(0, 20, 0, 0);
        buttonContainer.setLayoutParams(buttonContainerParams);

        // Restart Button
        Button restartButton = createActionButton(context, "Restart", "#3359DF"); // Blue color
        restartButton.setLayoutParams(new LinearLayout.LayoutParams(
                0, // Weight will make it expand
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Equal weight with close button
        ));
        restartButton.setOnClickListener(v -> showRestartSection(context));
        
        // Close Button
        Button closeButton = createActionButton(context, "Close", "#1B1F25"); // Dark color
        closeButton.setLayoutParams(new LinearLayout.LayoutParams(
                0, // Weight will make it expand
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Equal weight with restart button
        ));
        closeButton.setOnClickListener(v -> {
            if (currentDialog != null) currentDialog.dismiss();
        });

        // Add space between buttons
        LinearLayout.LayoutParams restartParams = (LinearLayout.LayoutParams) restartButton.getLayoutParams();
        restartParams.setMarginEnd(10);
        
        LinearLayout.LayoutParams closeParams = (LinearLayout.LayoutParams) closeButton.getLayoutParams();
        closeParams.setMarginStart(10);

        buttonContainer.addView(restartButton);
        buttonContainer.addView(closeButton);
        mainLayout.addView(buttonContainer);

        SettingsManager.saveAllFlags();

        Activity activity = InstagramUI.getCurrentActivity();
        if (activity != null) {
            InstagramUI.addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
        }

        return mainLayout;
    }

    // New helper method to create a settings item with icon, text and right arrow
    private static View createSettingsItem(Context context, String icon, String label, Runnable onClick) {
        LinearLayout itemLayout = new LinearLayout(context);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setGravity(Gravity.CENTER_VERTICAL);
        itemLayout.setPadding(20, 16, 20, 16);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create icon background
        LinearLayout iconContainer = new LinearLayout(context);
        iconContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dpToPx(context, 44), 
                dpToPx(context, 44)
        );
        iconParams.setMarginEnd(16);
        iconContainer.setLayoutParams(iconParams);

        // Icon background shape
        GradientDrawable iconBackground = new GradientDrawable();
        iconBackground.setShape(GradientDrawable.OVAL);
        iconBackground.setColor(Color.parseColor("#1F2226")); // Icon background color
        iconContainer.setBackground(iconBackground);

        // Icon text
        TextView iconView = new TextView(context);
        iconView.setText(icon);
        iconView.setTextSize(16);
        iconView.setGravity(Gravity.CENTER);
        iconContainer.addView(iconView);

        // Label text
        TextView labelView = new TextView(context);
        labelView.setText(label);
        labelView.setTextColor(Color.WHITE);
        labelView.setTextSize(16);
        labelView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Take remaining space
        ));

        // Right arrow
        TextView arrowView = new TextView(context);
        arrowView.setText(">");
        arrowView.setTextColor(Color.parseColor("#9E9E9E"));
        arrowView.setTextSize(18);
        arrowView.setPadding(8, 0, 0, 0);

        // Add all views to the layout
        itemLayout.addView(iconContainer);
        itemLayout.addView(labelView);
        itemLayout.addView(arrowView);

        // Make the whole layout clickable
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(Color.parseColor("#40FFFFFF")));
        states.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        itemLayout.setBackground(states);
        
        itemLayout.setOnClickListener(v -> onClick.run());
        
        return itemLayout;
    }

    // Helper method to create styled action buttons (Restart/Close)
    private static Button createActionButton(Context context, String text, String backgroundColor) {
        Button button = new Button(context);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(16);
        button.setAllCaps(false); // Makes text not all caps
        
        // Button shape with rounded corners
        GradientDrawable btnBackground = new GradientDrawable();
        btnBackground.setColor(Color.parseColor(backgroundColor));
        btnBackground.setCornerRadius(16);
        
        button.setBackground(btnBackground);
        button.setPadding(0, 16, 0, 16);
        
        return button;
    }
    
    // Helper method to convert dp to pixels
    private static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }