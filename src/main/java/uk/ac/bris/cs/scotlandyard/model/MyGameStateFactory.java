package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.frequency;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState>{

	/**
	 * Create an instance of the parameterised type given the parameters required for
	 * ScotlandYard game
	 *
	 * @param setup the game setup
	 * @param mrX MrX player
	 * @param detectives detective players
	 * @return an instance of the parameterised type
	 */
	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		return new MyGameState(setup, ImmutableSet.of(mrX.piece()/*.MRX*/), ImmutableList.of(), mrX, detectives);

	}


	private final class MyGameState implements GameState{
		private GameSetup setup; //to return it, as well as have access to the game graph and Mr X reveal moves
		private ImmutableSet<Piece> remaining; //to hold which pieces  can still move in current round,(just MrX at the starting round)
		private ImmutableList<LogEntry> log; // to hold the travel log and count the moves Mr has taken, (empty at the starting round)
		private Player mrX; //
		private List<Player> detectives; // list of detectives in the game
		private ImmutableSet<Move> AvMoves; //to hold the currently possible/available moves !!!changed moves to AvMoves!!!!
		private ImmutableSet<Piece> winner; // to hold the current winner(s)


		private MyGameState(final GameSetup setup, final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives){
			this.setup = checkNotNull(setup,"setup can't be null"); // from import static com.google.common.base.Preconditions.checkNotNull;
			this.remaining = checkNotNull(remaining,"remaining can't be null"); // throws NullPointerException - if reference is null
			this.log = checkNotNull(log,"Travel log(List<LogEntry>) can't be null");
			this.mrX = checkNotNull(mrX,"mrX(player) can't be null");
			this.detectives = checkNotNull(detectives,"detectives(List<Player>) can't be null");

			HashSet<Move> AMoves = new HashSet<>();


			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
			if(!mrX.isMrX()) throw new IllegalArgumentException("MrX created incorrectly, has to be black and can't be a detective");
			for(int i = 0; i<this.detectives.size(); i++) {
				if(this.detectives.get(i).isMrX()){throw new IllegalArgumentException("detective created incorrectly, swapped with MrX etc");}
				int fd = frequency(this.detectives, this.detectives.get(i));
				if(fd != 1 ){throw new IllegalArgumentException("detectives created incorrectly");}
				// detectives can't have secret or double tickets
				if(this.detectives.get(i).has(Ticket.SECRET) || detectives.get(i).has(Ticket.DOUBLE)){throw new IllegalArgumentException("detectives have illegal tickets");}
				for(int j = i+1; j<this.detectives.size();j++){
					if(this.detectives.get(j).location() == this.detectives.get(i).location()){
						throw new IllegalArgumentException("detectives locations overlap");
					}
				}
			}
			if(this.setup.graph.nodes().isEmpty() || this.setup.graph.edges().isEmpty()){
				throw new IllegalArgumentException("Graph can't be empty!");
			}

			for (Piece p : this.remaining) {
				AMoves.addAll(makeSingleMoves(this.setup, this.detectives, PieceGetPlayer(p).get(), PieceGetPlayer(p).get().location()));
				if (this.log.size() + 2 <= this.setup.moves.size()) { // check if enough rounds left to make a double move
					AMoves.addAll(makeDoubleMoves(this.setup, this.detectives, PieceGetPlayer(p).get(), PieceGetPlayer(p).get().location()));
				}
			}
			this.AvMoves = ImmutableSet.copyOf(AMoves);
			this.winner = determineWinner();
		}

		private ImmutableSet<Piece> determineWinner(){
			this.winner = ImmutableSet.of(); //testWinningPlayerIsEmptyInitially
			ArrayList<Piece> dP = new ArrayList<>();
			HashSet<Move> XM = new HashSet<>();

			var ref = new Object() {
				int counter = 0;
			};

			XM.addAll(makeSingleMoves(this.setup, this.detectives, this.mrX, this.mrX.location() ));
			if(this.log.size() +2 <= this.setup.moves.size()){
				XM.addAll(makeDoubleMoves(this.setup, this.detectives, this.mrX, this.mrX.location()));
			}

			for(Player p : detectives){
				dP.add(p.piece());
				if(p.tickets().values().stream().allMatch(x -> x == 0)){ ref.counter ++;}
				if(p.location() == this.mrX.location()){ ref.counter = -99999;} // mrx has been caught
			}
			if(ref.counter < 0){this.winner = ImmutableSet.copyOf(dP); return winner;} // mrx has been caught detectives win

			if(XM.isEmpty()){ // mrX can't move, detectives win
				this.winner = ImmutableSet.copyOf(dP);
				return winner;
			}

			if ( ref.counter == this.detectives.size() || this.log.size() == setup.moves.size() ){
				this.winner = ImmutableSet.of(this.mrX.piece());
				return winner;
			}

			return this.winner;
		}

		private Optional<Player> PieceGetPlayer(Piece piecee){
			var ref = new Object() {
				Player id = null;
			};

			if(piecee.isMrX()) {ref.id = this.mrX; }

			else if (piecee.isDetective()) {
				this.detectives.forEach((p) -> {
					if (p.piece() == piecee) {ref.id = p;}
				});
			}
			return Optional.ofNullable(ref.id);
		}

		/**
		 * @return the current game setup
		 */
		@Nonnull
		@Override
		public GameSetup getSetup() {return this.setup;}

		/**
		 * @return all players in the game /// ?currently in the game?
		 */
		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			List<Piece> DP = new ArrayList<>();
			for(int i = 0; i <this.detectives.size(); i++){DP.add(this.detectives.get(i).piece());}


			final ImmutableSet<Piece> GSFplayers =
					ImmutableSet.<Piece>builder()
							.add(this.mrX.piece())
							//.addAll( GSFdetectives.iterator() GSFdetectives.piece())
							//GSFdetectives.forEach((p) -> (List<Piece> DP =  p.piece()));
							.addAll(DP)
							.build();
			return GSFplayers;
		}

		/**
		 * @param detective the detective
		 * @return the location of the given detective; empty if the detective is not part of the game
		 */
		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			Player id = PieceGetPlayer(detective).orElse(null);
			if(id == null){return Optional.empty();}
			return Optional.ofNullable(id.location()); // ref.id can't be null,due to .location. otherwise ofNullable is sufficient
		}

		/**
		 * @param piece the player piece
		 * @return the ticket board of the given player; empty if the player is not part of the game
		 */
		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			Player id = PieceGetPlayer(piece).orElse(null);
			if(id == null){return Optional.empty();}

			TicketBoard GSFTB = new TicketBoard() {
				@Override
				public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
					/**
					 * @param ticket the ticket to check count for
					 * @return the amount of ticket,that player has, always &gt;>= 0
					 */
					return id.tickets().get(ticket);
				}
			};
			return Optional.ofNullable(GSFTB);
		}

		/**
		 * @return MrX's travel log as a list of {@link LogEntry}s.
		 */
		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() { return ImmutableList.copyOf(log);}

		/**
		 * @return the winner of this game; empty if the game has no winners yet
		 * This is mutually exclusive with {@link #getAvailableMoves()}
		 */
		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			return this.winner;
		}

		/**
		 * @return the current available moves of the game.
		 * This is mutually exclusive with {@link #getWinner()}
		 */
		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			if(!winner.isEmpty()){this.AvMoves = ImmutableSet.of();}
			return this.AvMoves;
		}

		private static Set<SingleMove> makeSingleMoves
				(GameSetup setup, List<Player> detectives, Player player, int source){
			HashSet<SingleMove> sMoves = new HashSet<>(); //type inference

			for(int destination1 : setup.graph.adjacentNodes(source)) { //int source = node, ie current piece position

				if(detectives.stream().noneMatch(x->x.location() == destination1)) { ///List.stream(detectives). ->

					for (Transport t1 : setup.graph.edgeValueOrDefault(source, destination1, ImmutableSet.of())) {
						if (player.has(t1.requiredTicket())) {
							sMoves.add(new SingleMove(player.piece(), source, t1.requiredTicket(), destination1));
						}
					}

					if (player.has(Ticket.SECRET)
							&& !((setup.graph.edgeValueOrDefault(source, destination1, ImmutableSet.of(Transport.FERRY))).contains(Transport.FERRY))) {  // could use stream.none match, also set default value to ferry just in case
						sMoves.add(new SingleMove(player.piece(), source, Ticket.SECRET, destination1));
					}
				}
			}
			return sMoves;
		}

		private static Set<DoubleMove> makeDoubleMoves
				(GameSetup setup, List<Player> detectives, Player player, int source){
			HashSet<DoubleMove> dMoves = new HashSet<>();
			ArrayList<SingleMove> firstM = new ArrayList<>(makeSingleMoves(setup, detectives, player, source));
			HashSet<SingleMove> secondM;
			if(/*player.isMrX() &&*/ player.has(Ticket.DOUBLE)) {
				for (SingleMove firstSM : firstM){
					secondM = new HashSet<>(makeSingleMoves(setup, detectives, player, firstSM.destination));
					for (SingleMove secondSM : secondM){
						if(secondSM.ticket == firstSM.ticket && player.hasAtLeast(secondSM.ticket, 2)){
							dMoves.add(new DoubleMove(player.piece(), source, firstSM.ticket, firstSM.destination, secondSM.ticket, secondSM.destination));
						}
						else if(secondSM.ticket != firstSM.ticket){
							dMoves.add(new DoubleMove(player.piece(), source, firstSM.ticket, firstSM.destination, secondSM.ticket, secondSM.destination));
						}
					}
				}
			}
			return dMoves;
		}

		/**
		 * Computes the next game state given a move from {@link #getAvailableMoves()} has been
		 * chosen and supplied as the parameter
		 *
		 * @param move the move to make
		 * @return the game state of which the given move has been made
		 * @throws IllegalArgumentException if the move was not a move from
		 *                                  {@link #getAvailableMoves()}
		 */
		@Nonnull
		@Override
		public GameState advance(Move move) {
			if(!AvMoves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			GameState NewGS = move.accept(new Visitor<GameState>(){
				int dm = 0;
				@Override public GameState visit(SingleMove singleMove){
					if(singleMove.commencedBy().isMrX()){
						ArrayList<LogEntry> LE = new ArrayList<>(log);
						if(setup.moves.get(LE.size())) {
							LE.add(LogEntry.reveal(singleMove.ticket, singleMove.destination) );
							log = ImmutableList.copyOf(LE);
						}
						else if(!setup.moves.get(LE.size())){
							LE.add(LogEntry.hidden(singleMove.ticket));
							log = ImmutableList.copyOf(LE);
						}
						mrX = mrX.at(singleMove.destination);
						if(dm == 0) { // if it is a double move, first move skip this
							ArrayList<Piece> DPieces = new ArrayList<>();
							for (Player p : detectives) {
								DPieces.add(p.piece());
							}
							remaining = ImmutableSet.copyOf(DPieces); // after mrX has moved change remaing to detectives
						}
						mrX = mrX.use(singleMove.ticket);
						return new MyGameState(getSetup(), remaining, log, mrX, detectives);
					}

					else if(singleMove.commencedBy().isDetective()){
						Player p = PieceGetPlayer(singleMove.commencedBy()).get();
						int a = 0;
						for(int i=0; i<detectives.size(); i++){
							if(detectives.get(i).piece() == singleMove.commencedBy()){
								a = i;
								break;
							}
						}
						p = p.at(singleMove.destination);
						p = p.use(singleMove.ticket);
						mrX = mrX.give(singleMove.ticket);
						ArrayList <Player> dAL = new ArrayList<>(detectives);
						dAL.add(a,p);
						dAL.remove(a+1);
						detectives = ImmutableList.copyOf(dAL);
						remaining = ImmutableSet.copyOf(remaining.stream().filter(x -> x !=singleMove.commencedBy()).toList());
						ArrayList<Integer> dt = new ArrayList<>();

						if(remaining.isEmpty() || remaining.stream().allMatch(x-> PieceGetPlayer(x).get().tickets().values().stream().allMatch(y -> y==0)  ) ){ // checks if detectives can still move, and switches remaining to mrX if requried
							remaining = ImmutableSet.of(mrX.piece());
						}
						return new MyGameState(getSetup(), remaining, log, mrX, detectives);

					}
					return null;
				}
				@Override public GameState visit(DoubleMove doubleMove){

					SingleMove FSM = new SingleMove(doubleMove.commencedBy(), doubleMove.source(), doubleMove.ticket1, doubleMove.destination1);
					SingleMove SSM = new SingleMove(doubleMove.commencedBy(), doubleMove.destination1, doubleMove.ticket2, doubleMove.destination2);
					mrX = mrX.use(Ticket.DOUBLE);

					dm = 1;
					GameState xd;
					xd = visit(FSM);
					dm = 0;
					xd = visit(SSM);
					return xd;
				}

			});
			if(NewGS == null){System.out.println("returned state is null");}
			return NewGS;
		}
	}
}