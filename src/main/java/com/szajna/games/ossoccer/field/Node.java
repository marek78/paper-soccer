package com.szajna.games.ossoccer.field;

public final class Node
{
    private final int x;
    private final int y;
    private final Edge edges[] = new Edge[8];
    private final boolean border;   // true if the node belongs to the border line.
    private final boolean goal;     // true if this is the goal node
    private boolean visited;        // true if any from the connected edges has been visited by any player or 
                                    // this is the start node, false otherwise.

    public Node(final int x, final int y, final boolean border, final boolean goal)
    {
        this.x = x;
        this.y = y;
        this.border = border;
        this.goal = goal;
        this.visited = false;
    }

    /**
     * Resets Edge to its initial (ready for a game) state.
     */
    public void reset()
    {
        visited = false;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public Edge getEdge(final int dir)
    {
        return edges[dir];
    }

    // true if movement should not stop at this point
    public boolean isMarked()
    {
        return border | visited;
    }

    public boolean isBorder()
    {
        return border;
    }

    public boolean isGoal()
    {
        return goal;
    }

    public void setVisited(final boolean visited)
    {
        this.visited = visited;
    }

    public boolean isVisited()
    {
        return visited;
    }

    public Node getNode(final int dir)
    {
        final Edge edge = edges[dir];
        return null != edge ? edge.getOpposite(this) : null;
    }

    void setEdge(final Edge edge, final int dir)
    {
        if (this != edge.getN1() && this != edge.getN2())
            throw new RuntimeException("Assigned edge doesn't point to the node.");

        if (null != edges[dir])
            throw new RuntimeException("New edge assigned to an already occupied direction.");

        edges[dir] = edge;
    }
}
