/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.view;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.slightlymagic.braids.util.UtilFunctions;
import net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor;

import com.esotericsoftware.minlog.Log;

import forge.AllZone;
import forge.ComputerAIGeneral;
import forge.ComputerAIInput;
import forge.Constant;
import forge.ImageCache;
import forge.control.ControlAllUI;
import forge.error.ErrorViewer;
import forge.game.GameType;
import forge.model.FModel;
import forge.properties.ForgePreferences;
import forge.view.home.SplashFrame;
import forge.view.toolbox.CardFaceSymbols;
import forge.view.toolbox.FSkin;

/**
 * The main view for Forge: a java swing application. All view class instances
 * should be accessible from here.
 */
public class FView {

    private transient SplashFrame splashFrame;

    /**
     * The splashFrame field is guaranteed to exist when this constructor exits.
     * 
     * @param skin
     *            the skin
     */
    public FView(final FSkin skin) {

        // We must use invokeAndWait here to fulfill the constructor's
        // contract.

        UtilFunctions.invokeInEventDispatchThreadAndWait(new Runnable() { // NOPMD
                                                                          // by
                                                                          // Braids
                                                                          // on
                                                                          // 8/18/11
                                                                          // 11:37
                                                                          // PM
                    @Override
                    public void run() {
                        FView.this.splashFrame = new SplashFrame(skin);
                    }
                });

        SwingUtilities.invokeLater(new Runnable() { // NOPMD by Braids on
                                                    // 8/18/11 11:37 PM
                    @Override
                    public void run() {
                        FView.this.splashFrame.setVisible(true);
                    }
                });

    }

    /**
     * Get the progress monitor for loading all cards at once.
     * 
     * @return a progress monitor having only one phase; may be null
     */
    public final BraidsProgressMonitor getCardLoadingProgressMonitor() {
        BraidsProgressMonitor result;

        if (this.splashFrame == null) {
            result = null;
        } else {
            result = this.splashFrame.getMonitorModel();
        }

        return result;
    }

    /**
     * Tell the view that the model has been bootstrapped, and its data is ready
     * for initial display.
     * 
     * @param model
     *            the model that has finished bootstrapping
     */
    public final void setModel(final FModel model) {
        try {

            final ForgePreferences preferences = model.getPreferences();

            // FindBugs doesn't like the next line.
            ImageCache.setScaleLargerThanOriginal(preferences.isScaleLargerThanOriginal());

        } catch (final Exception exn) {
            Log.error("Error loading preferences: " + exn);
        }

        // For the following two blocks, check if user has cancelled
        // SplashFrame.
        // Note: Error thrown sometimes because log file cannot be accessed
        if (!this.splashFrame.getSplashHasBeenClosed()) {
            AllZone.getCardFactory(); // forces preloading of all cards
        }

        if (!this.splashFrame.getSplashHasBeenClosed()) {
            try {
                CardFaceSymbols.loadImages();

                Constant.Runtime.setGameType(GameType.Constructed);
                SwingUtilities.invokeLater(new Runnable() { // NOPMD by Braids
                                                            // on 8/7/11 1:07
                                                            // PM: this isn't a
                                                            // web app
                            @Override
                            public void run() {
                                AllZone.getInputControl().setComputer(new ComputerAIInput(new ComputerAIGeneral()));

                                // Enable only one of the following two lines.
                                // The second
                                // is useful for debugging.

                                FView.this.splashFrame.dispose();
                                // splashFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                                FView.this.splashFrame = null;
                                AllZone.getSkin().loadFontAndImages();
                                GuiTopLevel g = new GuiTopLevel();
                                g.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                AllZone.setDisplay(g);
                                g.getController().changeState(ControlAllUI.HOME_SCREEN);
                            }
                        });
            } catch (final Exception ex) {
                ErrorViewer.showError(ex);
            }
        } // End if(splashHasBeenClosed)

    } // End FView()
}
