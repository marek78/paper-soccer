package com.szajna.games.ossoccer.field;

public final class Edge
{
    private final Node n1;
    private final Node n2;
    /**
     * true if the edge belongs to the border line.
     */
    private final boolean border;
    /**
     * One of PlayerId values indicating by whom was the edge visited, if was at all.
     */
    private int visitedBy;

    public Edge(final Node n1, final Node n2, final boolean border)
    {
        if (null == n1 || null == n2 || n1 == n2)
            throw new IllegalArgumentException();

        this.n1 = n1;
        this.n2 = n2;
        this.border = border;
        this.visitedBy = PlayerId.None;
    }

    /**
     * Resets Edge to its initial (ready for a game) state.
     */
    public void reset()
    {
        this.visitedBy = PlayerId.None;
    }

    public Node getN1()
    {
        return n1;
    }

    public Node getN2()
    {
        return n2;
    }

    public Node getOpposite(final Node node)
    {
        if (n1 != node && n2 != node)
            throw new IllegalArgumentException();

        return n1 != node ? n1 : n2;
    }

    public boolean isBlocked()
    {
        return border | (PlayerId.None != visitedBy);
    }

    public boolean isBorder()
    {
        return border;
    }

    public void setVisitedBy(final int visitedBy)
    {
        this.visitedBy = visitedBy;
    }

    public int getVisitedBy()
    {
        return visitedBy;
    }

    public boolean isVisited()
    {
        return PlayerId.None != visitedBy;
    }
}
