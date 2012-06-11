package magic.model.action;

import magic.model.MagicGame;
import magic.model.MagicPermanent;
import magic.model.MagicPermanentState;
import magic.model.MagicPlayer;
import magic.model.trigger.MagicTriggerType;
import magic.model.mstatic.MagicStatic;
import magic.model.mstatic.MagicLayer;

public class MagicGainControlAction extends MagicAction {

    private final MagicPlayer player;
    private final MagicPermanent permanent;
    private final boolean duration;
    
    public MagicGainControlAction(final MagicPlayer player,final MagicPermanent permanent) {
        this(player,permanent,MagicStatic.Forever);
    }
    
    public MagicGainControlAction(final MagicPlayer player,final MagicPermanent permanent,final boolean duration) {
        this.player = player;
        this.permanent = permanent;
        this.duration = duration;
    }
    
    @Override
    public void doAction(final MagicGame game) {
        //insert continous effect
        game.doAction(new MagicAddStaticAction(permanent, new MagicStatic(
                MagicLayer.Control,
                duration) {
            @Override
            public MagicPlayer getController(
                    final MagicGame game,
                    final MagicPermanent permanent,
                    final MagicPlayer controller) {
                return game.getPlayer(player.getIndex());
            }   
        }));
    }

    @Override
    public void undoAction(final MagicGame game) {
    }
}
