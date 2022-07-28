package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import java.util.ArrayList;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull
	@Override
	public Model build(GameSetup setup,
					   Player mrX,
					   ImmutableList<Player> detectives) {
		return new MyModel(setup, mrX, detectives);
	}

	private final class MyModel implements Model {
		private Board.GameState GS;
		private ArrayList<Observer> OL= new ArrayList<> ();

		// constructor
		private MyModel(GameSetup setup,
						Player mrX,
						ImmutableList<Player> detectives){
			this.GS = new MyGameStateFactory().build(setup, mrX, detectives);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			// TODO Advance the model with move, then notify all observers of what what just happened.
			//  you may want to use getWinner() to determine whether to send out Event.MOVE_MADE or Event.GAME_OVER
			this.GS =  GS.advance(move);
			if(GS.getWinner().isEmpty()) {
				for (Observer obs : OL) {
					obs.onModelChanged(this.GS, Observer.Event.MOVE_MADE);
				}
			}
			else {
				for (Observer obs : OL) {
					obs.onModelChanged(this.GS, Observer.Event.GAME_OVER);
				}
			}
		}
		/**
		 * @return the current game board
		 */
		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return this.GS;
		}

		/**
		 * Registers an observer to the model. It is an error to register the same observer more than
		 * once.
		 *
		 * @param observer the observer to register
		 */
		@Override
		public void registerObserver(@Nonnull Observer observer) {
			if (observer.equals(null)) throw new NullPointerException();
			if (OL.contains(observer)) throw new IllegalArgumentException();
			this.OL.add(observer);

		}

		/**
		 * Unregisters an observer to the model. It is an error to unregister an observer not
		 * previously registered with {@link #registerObserver(Observer)}.
		 *
		 * @param observer the observer to register
		 */
		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			if (observer.equals(null)) throw new NullPointerException();
			if(!this.OL.remove(observer)) throw new IllegalArgumentException();
			this.OL.remove(observer);
		}

		/**
		 * @return all currently registered observers of the model
		 */
		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			ImmutableSet<Observer> IOL = ImmutableSet.copyOf(OL);
			return IOL;
		}

	}
}

