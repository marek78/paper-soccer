package com.szajna.games.ossoccer.field;

import java.util.ArrayList;

public final class Path
{
    /**
     * PathElement - the Path is build of PathElement objects. For optimization reasons static method
     * valueOf() is provided. Immutable.
     */
    public static final class PathElement
    {
        public static final PathElement PATH_SEPARATOR = new PathElement();

        public static final PathElement valueOf(final byte moveDirection, final byte playerId)
        {
            if (moveDirection < Move.DIRECTION_0 || Move.DIRECTION_7 < moveDirection)
                throw new IllegalArgumentException();
            if (PlayerId.Player1 != playerId && PlayerId.Player2 != playerId)
                throw new IllegalArgumentException();

            return PATH_ELEMENTS[playerId - PlayerId.Player1][moveDirection];
        }

        public boolean isSeparator()
        {
            return SEPARATOR == (SEPARATOR & data);
        }

        public byte getMoveDirection()
        {
            return (byte) (data & Move.DIRECTION_MASK);
        }

        public byte getPlayerId()
        {
            return (byte) (((data & PLAYER_MASK) >>> 3) + PlayerId.Player1);
        }

        private static final PathElement DIR0_PID1 = new PathElement(Move.DIRECTION_0, (byte) PlayerId.Player1);
        private static final PathElement DIR1_PID1 = new PathElement(Move.DIRECTION_1, (byte) PlayerId.Player1);
        private static final PathElement DIR2_PID1 = new PathElement(Move.DIRECTION_2, (byte) PlayerId.Player1);
        private static final PathElement DIR3_PID1 = new PathElement(Move.DIRECTION_3, (byte) PlayerId.Player1);
        private static final PathElement DIR4_PID1 = new PathElement(Move.DIRECTION_4, (byte) PlayerId.Player1);
        private static final PathElement DIR5_PID1 = new PathElement(Move.DIRECTION_5, (byte) PlayerId.Player1);
        private static final PathElement DIR6_PID1 = new PathElement(Move.DIRECTION_6, (byte) PlayerId.Player1);
        private static final PathElement DIR7_PID1 = new PathElement(Move.DIRECTION_7, (byte) PlayerId.Player1);

        private static final PathElement DIR0_PID2 = new PathElement(Move.DIRECTION_0, (byte) PlayerId.Player2);
        private static final PathElement DIR1_PID2 = new PathElement(Move.DIRECTION_1, (byte) PlayerId.Player2);
        private static final PathElement DIR2_PID2 = new PathElement(Move.DIRECTION_2, (byte) PlayerId.Player2);
        private static final PathElement DIR3_PID2 = new PathElement(Move.DIRECTION_3, (byte) PlayerId.Player2);
        private static final PathElement DIR4_PID2 = new PathElement(Move.DIRECTION_4, (byte) PlayerId.Player2);
        private static final PathElement DIR5_PID2 = new PathElement(Move.DIRECTION_5, (byte) PlayerId.Player2);
        private static final PathElement DIR6_PID2 = new PathElement(Move.DIRECTION_6, (byte) PlayerId.Player2);
        private static final PathElement DIR7_PID2 = new PathElement(Move.DIRECTION_7, (byte) PlayerId.Player2);

        private static final PathElement PATH_ELEMENTS[][] = {
                { DIR0_PID1, DIR1_PID1, DIR2_PID1, DIR3_PID1, DIR4_PID1, DIR5_PID1, DIR6_PID1, DIR7_PID1 },
                { DIR0_PID2, DIR1_PID2, DIR2_PID2, DIR3_PID2, DIR4_PID2, DIR5_PID2, DIR6_PID2, DIR7_PID2 }, };

        private static final byte SEPARATOR = 0x10;
        private static final byte PLAYER_MASK = 0x08;

        private final byte data;

        private PathElement()
        {
            data = SEPARATOR;
        }

        private PathElement(final byte moveDirection, final byte playerId)
        {

            if (moveDirection < Move.DIRECTION_0 || Move.DIRECTION_7 < moveDirection)
                throw new IllegalArgumentException();
            if (PlayerId.Player1 != playerId && PlayerId.Player2 != playerId)
                throw new IllegalArgumentException();

            data = (byte) (moveDirection | ((playerId - PlayerId.Player1) << 3));
        }
    };

    private final ArrayList<PathElement> moves;

    public Path()
    {
        moves = new ArrayList<PathElement>();
    }

    public void clear()
    {
        moves.clear();
    }

    public void addMove(PathElement move)
    {
        moves.add(move);
    }

    public void addPath(final Path path)
    {
        for (PathElement move : path.moves)
        {
            moves.add(move);
        }
    }

    public ArrayList<PathElement> getMoves()
    {
        return moves;
    }

    public static Path getDiff(final Path p1, final Path p2)
    {
        final int mc1 = p1.moves.size();
        final int mc2 = p2.moves.size();
        final int mds = mc1 < mc2 ? mc1 : mc2;
        final int mdc = mc1 < mc2 ? mc2 - mds : mc1 - mds;
        final ArrayList<PathElement> md = mc1 < mc2 ? p2.moves : p1.moves;

        final Path diffPath = new Path();
        final ArrayList<PathElement> diffMoves = diffPath.getMoves();

        for (int i = 0; i < mdc; ++i)
        {
            diffMoves.add(md.get(mds + i));
        }

        return diffPath;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(moves.size());
        sb.append(" move(s): ");

        for (PathElement e : moves)
        {
            sb.append("[");
            if (e.isSeparator())
            {
                sb.append("|");
            }
            else
            {
                sb.append("d");
                sb.append(e.getMoveDirection());
                sb.append(",p");
                sb.append(e.getPlayerId());
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
