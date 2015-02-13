package fvs.taxe.controller;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;
import java.util.List;

import fvs.taxe.StationClickListener;
import fvs.taxe.TaxeGame;
import gameLogic.GameState;
import gameLogic.map.CollisionStation;
import gameLogic.map.IPositionable;
import gameLogic.map.Position;
import gameLogic.map.Station;
import gameLogic.resource.Train;

public class RouteController {
    private Context context;
    private Group routingButtons = new Group();
    private List<IPositionable> positions;
    private boolean isRouting = false;
    private Train train;
    private boolean canEndRouting = true;
    public RouteController(Context context) {
        this.context = context;

        StationController.subscribeStationClick(new StationClickListener() {
            @Override
            public void clicked(Station station) {
                if (isRouting) {
                    addStationToRoute(station);
                }
            }
        });
    }

    public void begin(Train train) {
        this.train = train;
        isRouting = true;
        positions = new ArrayList<IPositionable>();
        positions.add(train.getPosition());
        context.getGameLogic().setState(GameState.ROUTING);
        addRoutingButtons();

        TrainController trainController = new TrainController(context);
        trainController.setTrainsVisible(train, false);
        train.getActor().setVisible(true);
    }

    public void begin2(Train train) {
        this.train = train;
        isRouting = true;
        positions = new ArrayList<IPositionable>();
        /*String previousStationName = train.getHistory().get(train.getHistory().size()-1).getFirst();
        Station previousStation = context.getGameLogic().getMap().getStationByName(previousStationName);
        positions.add(previousStation.getLocation());*/
        context.getGameLogic().setState(GameState.ROUTING);
        addRoutingButtons();

        TrainController trainController = new TrainController(context);
        trainController.setTrainsVisible(train, false);
        train.getActor().setVisible(true);
    }

    private void addStationToRoute(Station station) {
        // the latest position chosen in the positions so far
        IPositionable lastPosition = null;
        int proceed = 0;
        try{
            lastPosition =  positions.get(positions.size() - 1);
            proceed = 1;
        }
        catch(Exception e){}
        if (proceed==1) {

            Station lastStation = context.getGameLogic().getMap().getStationFromPosition(lastPosition);

            boolean hasConnection = context.getGameLogic().getMap().doesConnectionExist(station.getName(), lastStation.getName());
            //Check whether a connection exists
            if (!hasConnection) {
                context.getTopBarController().displayFlashMessage("This connection doesn't exist", Color.RED);
            } else {
                positions.add(station.getLocation());
                canEndRouting = !(station instanceof CollisionStation);
            }

        }
        if (positions.size() == 0) {
            positions.add(station.getLocation());
        }
        return;
    }

    private void addRoutingButtons() {
        TextButton doneRouting = new TextButton("Route Complete", context.getSkin());
        TextButton cancel = new TextButton("Cancel", context.getSkin());

        doneRouting.setPosition(TaxeGame.WIDTH - 250, TaxeGame.HEIGHT - 33);
        cancel.setPosition(TaxeGame.WIDTH - 100, TaxeGame.HEIGHT - 33);

        cancel.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                endRouting();
            }
        });

        doneRouting.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if(!canEndRouting) {
                    context.getTopBarController().displayFlashMessage("Your route must end at a station", Color.RED);
                    return;
                }

                confirmed();
                endRouting();
            }
        });

        routingButtons.addActor(doneRouting);
        routingButtons.addActor(cancel);

        context.getStage().addActor(routingButtons);
    }

    private void confirmed() {
        train.setRoute(context.getGameLogic().getMap().createRoute(positions));
        TrainMoveController move = new TrainMoveController(context,train);
    }

    private void endRouting() {
        context.getGameLogic().setState(GameState.NORMAL);
        routingButtons.remove();
        isRouting = false;

    TrainController trainController = new TrainController(context);
    trainController.setTrainsVisible(train, true);
        //TODO: FIX TRAIN NOT BEING HIDDEN HERE
        if (train.getRoute().size()==0){
            train.getActor().setVisible(false);
        }
    }

    public void drawRoute(Color color) {
        TaxeGame game = context.getTaxeGame();

        IPositionable previousPosition = null;
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(color);
        if (train.getPosition().getX()==-1&&positions.size()>0){
            Rectangle trainBounds = train.getActor().getBounds();
                game.shapeRenderer.rectLine(trainBounds.getX()+(trainBounds.getWidth()/2), trainBounds.getY()+(trainBounds.getWidth()/2), positions.get(0).getX(),
                        positions.get(0).getY(), StationController.CONNECTION_LINE_WIDTH);
        }
        for(IPositionable position : positions) {
            if(previousPosition != null) {
                game.shapeRenderer.rectLine(previousPosition.getX(), previousPosition.getY(), position.getX(),
                        position.getY(), StationController.CONNECTION_LINE_WIDTH);
            }

            previousPosition = position;
        }

        game.shapeRenderer.end();
    }

    public void viewRoute(Train train) {
        routingButtons.clear();

        train.getRoute();

        //isRouting = true;
        positions = new ArrayList<IPositionable>();

        for (Station station : train.getRoute()){
            positions.add(station.getLocation());

        }

        context.getGameLogic().setState(GameState.ROUTING);


        TextButton back = new TextButton("Return", context.getSkin());

        back.setPosition(TaxeGame.WIDTH - 100, TaxeGame.HEIGHT - 33);

        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                context.getGameLogic().setState(GameState.NORMAL);
                context.getGameLogic().setState(GameState.NORMAL);
                routingButtons.remove();

            }
        });

        routingButtons.addActor(back);

        context.getStage().addActor(routingButtons);
    }

}
