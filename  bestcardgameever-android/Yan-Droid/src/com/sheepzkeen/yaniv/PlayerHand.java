package com.sheepzkeen.yaniv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PlayerHand extends Hand {

	
	

	public PlayerHand(View container, ImageView[] cards, TextView name) {
		super(container,cards,name);
		//cards always visible for p1
		setShouldCardsBeShown(true);
	}

	

	//should Override
	@Override
	protected void selectCardsToDrop() throws InvalidYanivException {
		//Do nothing here, selection was performed by the drop action
		verifyCardsToDrop();
	}







	private void verifyCardsToDrop() throws InvalidYanivException {
		
		//Verify algorithm:
		// 1) remove jokers, add 1 to jokerCount for each joker
		// 2) if there is zero to one card only - allow
		// 3) check, if all cards are of different suits:
		// 3.	1) check for each card, if value = preceding value, allow, else reject 
		// 4) in case they are of the same suit:
		// 4.	1) if cards.count<3 , check for jokers, if exist - continue, else reject
		// 4.	2) remove aces, put aside
		// 4.	3) sort hand from highest to low
		// 4.	4) iterate over it, for each card - compare to next card 
		// 4.	4.	1) if this card and the next card have a difference of exactly 1 - continue, else:
		// 4.	4.	1.	2) (if difference>1) check that difference-1 <= jokerCount, if so, subtract difference-1 from jokerCount
		// 5) see if ace existed, if so, check if it can be attached to end\start (check highest card + jokercount == 14 or lowest card - jokerCount == 1)

		//0 - initialize
		int jokerCount = 0;
		boolean suitsAreDifferent = false;
		Set<Character> suitsInCards = new HashSet<Character>();
		Set<PlayingCard> acesInCards = new HashSet<PlayingCard>();
		ArrayList<PlayingCard> cardsToCheck  = (ArrayList<PlayingCard>) Arrays.asList(cards);
		//1
		for (PlayingCard card : cardsToCheck) {
			if(card.getIntegerValue() == null){
				jokerCount++;
				cardsToCheck.remove(card);
			}
		}
		//2
		if(cardsToCheck.size()<=1){
			//allow
			return;
		}
		//3	
		for (PlayingCard card : cardsToCheck) {
			if(suitsInCards.contains(card.getSuit())){
				//this suit has already appeared once
				suitsAreDifferent = true;
			}
		}
		
		//3.1
		PlayingCard lastCardChecked = null;
		if(suitsAreDifferent){
			for (PlayingCard card : cardsToCheck) {
				if(lastCardChecked != null && lastCardChecked.getIntegerValue() != card.getIntegerValue())
					//Reject
					throw new InvalidYanivException("Cards of different suits must have same value!");
			}
		}else{
			//4
			//4.1
			if(cardsToCheck.size() < 3 && jokerCount == 0){
				//Reject
				throw new InvalidYanivException("Cards have same suit, but are not enough to complete a series! " +
						"(only " + cardsToCheck.size() + " cards dropped and no jokers)");
			}
			//4.2
			for (PlayingCard card : cardsToCheck) {
				if(card.getIntegerValue() == PlayingCard.ACE){
					acesInCards.add(card);
					cardsToCheck.remove(card);
				}
			}
			//4.3
			Collections.sort(cardsToCheck);
			//4.4
			for (int i = 0; i < cardsToCheck.size()-1; i++) {
				int differenceBetweenThisAndNextCardVal =
					cardsToCheck.get(i).getIntegerValue() - cardsToCheck.get(i+1).getIntegerValue();
				//4.4.1
				if( differenceBetweenThisAndNextCardVal != 1){
					//4.4.1.2
					if(differenceBetweenThisAndNextCardVal - 1 > jokerCount){
						//reject
						throw new InvalidYanivException("Cards have same suit, " +
								"but the difference between them is too big, even with jokers. " +
								"(cards: "+ cardsToCheck.get(i) + " and " + cardsToCheck.get(i+1)+")");
					}else{
						jokerCount = jokerCount - differenceBetweenThisAndNextCardVal - 1;
					}
				}
					
			}
		}		
		//5
		if(!acesInCards.isEmpty()){
			//highest
			if((cardsToCheck.get(0).getIntegerValue() +jokerCount == 14) ||
					(cardsToCheck.get(cardsToCheck.size()).getIntegerValue() -jokerCount == 1)){
				//OK
			}else{
				//reject
				throw new InvalidYanivException("Cards have at least one ace that cannot be attached to series.");
			}
		}
	}



	@Override
	public void pickup(PlayingCard card) {
		addCard(card);
	}
	
	

	/**
	 * returns true iff the player is currently in a state when he can pickup a card (from the thrown or deck)
	 * @return True iff the player is currently in a state when he can pickup a card (from the thrown or deck)
	 */
	public boolean canPickup() {
		return (firstFreeLocation != Yaniv.YANIV_NUM_CARDS);
	}
	
	@Override
	public boolean isAwaitingInput() {
		return true;
	}
	

}