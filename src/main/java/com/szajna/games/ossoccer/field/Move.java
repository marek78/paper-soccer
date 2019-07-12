package com.szajna.games.ossoccer.field;

public final class Move 
{
    /**
     * Move directions;
     * 0 at north (0 degree); increased clockwise by 45 degree.
     *  7  0  1
     *   \ | /
     * 6 - - - 2
     *   / | \
     *  5  4  3
     */
    public static final byte DIRECTION_0 = 0;
    public static final byte DIRECTION_1 = 1;
    public static final byte DIRECTION_2 = 2;
    public static final byte DIRECTION_3 = 3;
    public static final byte DIRECTION_4 = 4;
    public static final byte DIRECTION_5 = 5;
    public static final byte DIRECTION_6 = 6;
    public static final byte DIRECTION_7 = 7;

    public static final byte DIRECTION_MASK = 0x07;
    public static final byte DIRECTION_MODULO = 8;

    /**
     * Get opposite move direction.
     * 
     * @param direction
     * @return opposite move direction.
     */
    public static int getOppositeDirection(int direction) throws IllegalArgumentException
    {
        if (direction < DIRECTION_0 || direction > DIRECTION_7)
            throw new IllegalArgumentException();

        return (direction + 4) % DIRECTION_MODULO;
    }
}
