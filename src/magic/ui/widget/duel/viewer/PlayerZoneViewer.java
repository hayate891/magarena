package magic.ui.widget.duel.viewer;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import magic.data.MagicIcon;
import magic.model.MagicCardList;
import magic.model.MagicPlayerZone;
import magic.translate.MText;
import magic.translate.StringContext;
import magic.ui.MagicImages;
import magic.ui.ScreenController;
import magic.ui.duel.viewerinfo.PlayerViewerInfo;
import magic.ui.helpers.ImageHelper;
import magic.ui.screen.duel.game.SwingGameController;
import magic.ui.widget.TabSelector;
import magic.utility.MagicSystem;

@SuppressWarnings("serial")
public class PlayerZoneViewer extends JPanel implements ChangeListener {

    private static final Map<MagicIcon, ImageIcon> tabIcons = getTabIcons();

    // translatable strings
    @StringContext(eg = "as in 'Other' player zone")
    private static final String _S1 = "Choose a card";
    private static final String _S2 = "Other : %s";
    private static final String _S3 = "%s Hand";
    private static final String _S4 = "%s Graveyard";
    private static final String _S5 = "%s Exile";

    // fired when contents of player zone is changed
    public static final String CP_PLAYER_ZONE = "ece91afb-3290-463e-ad03-8a24731f2aa0";

    private final SwingGameController controller;
    private final TabSelector tabSelector;
    private final MagicCardList cardsToChoose = new MagicCardList();
    private JToggleButton selectedTab = null;
    private final ImageCardListViewer imageCardsListViewer;

    private static Map<MagicIcon, ImageIcon> getTabIcons() {
        Map<MagicIcon, ImageIcon> icons = new HashMap<>();
        icons.put(MagicIcon.HAND_ZONE, getTabIcon(MagicIcon.HAND_ZONE));
        icons.put(MagicIcon.GRAVEYARD_ZONE, getTabIcon(MagicIcon.GRAVEYARD_ZONE));
        icons.put(MagicIcon.EXILE_ZONE, getTabIcon(MagicIcon.EXILE_ZONE));
        icons.put(MagicIcon.LIBRARY_ZONE, getTabIcon(MagicIcon.LIBRARY_ZONE));
        return icons;
    }

    private static ImageIcon getTabIcon(MagicIcon icon) {
        ImageIcon handIcon = MagicImages.getIcon(icon);
        BufferedImage handImage = ImageHelper.getBufferedImage(handIcon);
        BufferedImage scaledImage = ImageHelper.scale(handImage, 16, 16);
        return new ImageIcon(scaledImage);
    }

    public PlayerZoneViewer(final SwingGameController controller) {

        this.controller = controller;
        this.imageCardsListViewer = new ImageCardListViewer(controller);

        setOpaque(false);
        setLayout(new BorderLayout(6, 0));

        tabSelector = new TabSelector(this);
        tabSelector.addTab(tabIcons.get(MagicIcon.HAND_ZONE), "");
        tabSelector.addTab(tabIcons.get(MagicIcon.GRAVEYARD_ZONE), "");
        tabSelector.addTab(tabIcons.get(MagicIcon.GRAVEYARD_ZONE), "");
        tabSelector.addTab(tabIcons.get(MagicIcon.EXILE_ZONE), "");
        tabSelector.addTab(tabIcons.get(MagicIcon.EXILE_ZONE), "");
        tabSelector.addTab(tabIcons.get(MagicIcon.LIBRARY_ZONE), "");
        // this is used if the players are switched (ie. using the 'S' key).
        tabSelector.addTab(tabIcons.get(MagicIcon.LIBRARY_ZONE), "");
//        add(tabSelector, BorderLayout.WEST);

        add(imageCardsListViewer, BorderLayout.CENTER);
    }

    private void setSelectedTab(final int selectedTab, final boolean showFullScreen) {
        if (selectedTab >= 0) {
            tabSelector.setSelectedTab(selectedTab, showFullScreen);
        }
    }

    public void setSelectedTab(final int selectedTab) {
        setSelectedTab(selectedTab, false);
    }

    public void showCardsToChoose(MagicCardList cards) {
        cardsToChoose.clear();
        cardsToChoose.addAll(cards);
        setSelectedTab(5);
    }

    public void update() {
        update(false);
    }

    private void showCards(MagicCardList cards, boolean isFullScreen, String cardZoneTitle, CardImageViewerMode mode) {
        if (isFullScreen) {
            showFullScreenZone(cards, cardZoneTitle);
        } else {
            imageCardsListViewer.setCardList(cards, mode);
            firePropertyChange(CP_PLAYER_ZONE, null, cardZoneTitle);
        }
    }

    private String getZoneName(PlayerViewerInfo player, MagicPlayerZone zone) {
        switch (zone) {
            case HAND: return MText.get(_S3, player.getName());
            case GRAVEYARD: return MText.get(_S4, player.getName());
            case EXILE: return MText.get(_S5, player.getName());
        }
        throw new RuntimeException("Unsupported MagicPlayerZone : " + zone);
    }

    private void update(boolean showFullScreen) {
        switch (tabSelector.getSelectedTab()) {
            case 0: // main player hand
                showCards(
                    getUserPlayer().hand,
                    showFullScreen,
                    !showFullScreen && !getUserPlayer().isAi()
                        ? ""
                        : getZoneName(getUserPlayer(), MagicPlayerZone.HAND),
                    CardImageViewerMode.DECORATED
                );
                break;
            case 1: // main player graveyard
                showCards(getUserPlayer().graveyard,
                    showFullScreen,
                    getZoneName(getUserPlayer(), MagicPlayerZone.GRAVEYARD),
                    CardImageViewerMode.PLAIN
                );
                break;
            case 2: // opponent graveyard
                showCards(getAiPlayer().graveyard,
                    showFullScreen,
                    getZoneName(getAiPlayer(), MagicPlayerZone.GRAVEYARD),
                    CardImageViewerMode.PLAIN
                );
                break;
            case 3: // main player exile
                showCards(getUserPlayer().exile,
                    showFullScreen,
                    getZoneName(getUserPlayer(), MagicPlayerZone.EXILE),
                    CardImageViewerMode.PLAIN
                );
                break;
            case 4: // opponent exile
                showCards(getAiPlayer().exile,
                    showFullScreen,
                    getZoneName(getAiPlayer(), MagicPlayerZone.EXILE),
                    CardImageViewerMode.PLAIN
                );
                break;
            case 5:// main player choice
                showCards(cardsToChoose,
                    showFullScreen,
                    MText.get(_S1),
                    CardImageViewerMode.PLAIN
                );
                break;
            case 6: // opponent hand
                showCards(
                    getAiPlayer().hand,
                    showFullScreen,
                    getZoneName(getAiPlayer(), MagicPlayerZone.HAND),
                    getUserPlayer().isAi() || MagicSystem.isDevMode() || MagicSystem.isTestGame()
                        ? CardImageViewerMode.PLAIN
                        : CardImageViewerMode.FACEDOWN
                );
                break;
        }
        repaint();
    }

    private void showFullScreenZone(final MagicCardList aCardList, final String zoneName) {
        ScreenController.showCardZoneScreen(aCardList, zoneName, false);
    }

    @Override
    public void stateChanged(final ChangeEvent event) {

        final JToggleButton btn = (JToggleButton) event.getSource();
        final boolean zoneChanged = selectedTab == null || !selectedTab.getActionCommand().equals(btn.getActionCommand());
        final boolean showFullScreenZone = !zoneChanged && tabSelector.isUserClick();

        if (zoneChanged || showFullScreenZone) {
            update(showFullScreenZone);
            if (zoneChanged) {
                notifyPlayerZoneListeners(Integer.parseInt(btn.getActionCommand()));
            }
        }

        this.selectedTab = btn;

    }

    public void setPlayerZone(final PlayerViewerInfo playerInfo, final MagicPlayerZone zone) {
        final int tabIndex = getZoneButtonIndex(playerInfo, zone);
        final boolean showFullScreen = tabSelector.getSelectedTab() == tabIndex;
        setSelectedTab(tabIndex, showFullScreen);
    }

    private void notifyPlayerZoneListeners(final int newPlayerZoneIndex) {
        if (newPlayerZoneIndex == 0) {
            controller.notifyPlayerZoneChanged(getUserPlayer(), MagicPlayerZone.HAND);
        } else if (newPlayerZoneIndex == 1) {
            controller.notifyPlayerZoneChanged(getUserPlayer(), MagicPlayerZone.GRAVEYARD);
        } else if (newPlayerZoneIndex == 2) {
            controller.notifyPlayerZoneChanged(getAiPlayer(), MagicPlayerZone.GRAVEYARD);
        } else if (newPlayerZoneIndex == 3) {
            controller.notifyPlayerZoneChanged(getUserPlayer(), MagicPlayerZone.EXILE);
        } else if (newPlayerZoneIndex == 4) {
            controller.notifyPlayerZoneChanged(getAiPlayer(), MagicPlayerZone.EXILE);
        } else if (newPlayerZoneIndex == 5) {
            controller.notifyPlayerZoneChanged(getUserPlayer(), MagicPlayerZone.CHOICE);
        } else if (newPlayerZoneIndex == 6) {
            controller.notifyPlayerZoneChanged(getAiPlayer(), MagicPlayerZone.HAND);
        }
    }

    /**
     * Returns the latest instance of PlayerViewInfo.
     * <p>
     * <b>Do not retain a reference toPlayerViewerInfo since a new instance
     * is created whenever ViewerInfo is updated.</b>
     */
    private PlayerViewerInfo getUserPlayer() {
        return controller.getGameViewerInfo().getMainPlayer();
    }

    /**
     * Returns the latest instance of PlayerViewInfo.
     * <p>
     * <b>Do not retain a reference toPlayerViewerInfo since a new instance
     * is created whenever ViewerInfo is updated.</b>
     */
    private PlayerViewerInfo getAiPlayer() {
        return controller.getGameViewerInfo().getOpponent();
    }

    private int getZoneButtonIndex(PlayerViewerInfo playerInfo, MagicPlayerZone zone) {
        if (playerInfo.player == getUserPlayer().player) {
            switch (zone) {
                case HAND:
                    return 0;
                case GRAVEYARD:
                    return 1;
                case EXILE:
                    return 3;
                case LIBRARY:
                    return 5;
                default:
                    throw new RuntimeException("No zone viewer available!");
            }
        } else {
            switch (zone) {
                case HAND:
                    return 6;
                case GRAVEYARD:
                    return 2;
                case EXILE:
                    return 4;
                default:
                    throw new RuntimeException("No zone viewer available!");
            }
        }
    }

    public ImageCardListViewer getImageCardsListViewer() {
        return imageCardsListViewer;
    }

    public void switchPlayerZone() {
        switch (Integer.parseInt(selectedTab.getActionCommand())) {
            case 0: // P0 hand
                setSelectedTab(6);
                break;
            case 1: // P0 graveyard
                setSelectedTab(2);
                break;
            case 2: // P1 graveyard
                setSelectedTab(1);
                break;
            case 3: // P0 exile
                setSelectedTab(4);
                break;
            case 4: // P1 exile
                setSelectedTab(3);
                break;
            case 6: // P1 hand
                setSelectedTab(0);
                break;
            default:
                // do ntohing
        }
    }

    public void showChoiceViewerIfActive() {
        if (!cardsToChoose.isEmpty()) {
            setSelectedTab(5);
        }
    }

}
