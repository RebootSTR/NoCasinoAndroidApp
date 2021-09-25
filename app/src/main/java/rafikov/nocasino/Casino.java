package rafikov.nocasino;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Casino {

    private Queue<int[]> cache1;
    private Queue<int[]> cache2;
    private Queue<int[]> cache3;

    private final int winMultiplier1 = 50;
    private final int winMultiplier9 = 5;
    private final int winMultiplier90 = 0;

    private int cost = 100;

    private int lastWin;

    private int money;

    public int getMoney() {
        return money;
    }

    public Casino(int startMoney) {
        cache1 = new LinkedList<>();
        cache2 = new LinkedList<>();
        cache3 = new LinkedList<>();
        cache3.add(new int[] {5,5,5});
        money = startMoney;
    }

    public int getLastWin() {
        return lastWin;
    }

    public int setCost(int newCost) {
        if (newCost > 0) {
            cost = newCost;
        }
        return cost;
    }

    public int getCost() {
        return cost;
    }

    /**
     * Do a spin in casino if enough money and return slots.
     * @return set of int slots
     * @throws NoMoneyException if money is not enough
     */
    public int[] doSpin() {
        if (money < cost) {
            throw new NoMoneyException();
        }
        money -= cost;
        Random random = new Random();
        generateSlots(random);

        int chance = random.nextInt(100);
        Queue<int[]> cache;
        if (chance == 0) { // 1%
            cache = cache3;
            lastWin = cost*winMultiplier1;
        } else if (chance <= 9) { // 9%
            cache = cache2;
            lastWin = cost*winMultiplier9;
        } else { // 90%
            cache = cache1;
            lastWin = cost*winMultiplier90;
        }
        while (cache.size() == 0) {
            generateSlots(random);
        }
        money += lastWin;
        return cache.poll();
    }

    private void generateSlots(Random random) {
        int[] slots = new int[] {
                random.nextInt(10),
                random.nextInt(10),
                random.nextInt(10)
        };
        int countSlotsEquals = getCountSlotsEquals(slots);
        switch (countSlotsEquals) {
            case 3:
                cache3.add(slots);
                break;
            case 2:
                cache2.add(slots);
                break;
            case 1:
                cache1.add(slots);
                break;
        }
    }

    private int getCountSlotsEquals(int[] slots) {
        if (slots[0] == slots[1] && slots[1] == slots[2]) {
            return 3;
        } else if (slots[0] == slots[1] ||
                slots[0] == slots[2] ||
                slots[1] == slots[2]) {
            return 2;
        } else {
            return 1;
        }
    }

}
