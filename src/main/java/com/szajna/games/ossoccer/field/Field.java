package com.szajna.games.ossoccer.field;

import java.util.ArrayList;

import com.szajna.games.ossoccer.field.Path.PathElement;
import com.szajna.util.Log;

public final class Field
{
    private static final String LOG_TAG = Field.class.getSimpleName();
    private final Path path = new Path();
    private final int width;
    private final int height;
    private final Node[] nodes;
    private final Node start;
    private Node current;

    Field(final int width, final int height, final Node[] nodes, final Node start)
    {
        this.width = width;
        this.height = height;
        this.nodes = nodes;
        this.start = start;
        this.current = this.start;
    }

    /**
     * Resets Edge to its initial (ready for a game) state.
     */
    public void reset()
    {
        this.path.clear();

        for (Node node : nodes)
        {
            if (null != node)
            {
                node.reset();

                for (int i = Move.DIRECTION_0; i <= Move.DIRECTION_7; ++i)
                {
                    final Edge edge = node.getEdge(i);

                    if (null != edge)
                    {
                        edge.reset();
                    }
                }
            }
        }

        this.current = this.start;
        this.current.setVisited(true);
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public Node getCurrent()
    {
        return current;
    }

    public void setCurrent(final int x, final int y)
    {
        this.current = nodes[y * width + x];
    }

    public Node getNode(final int x, final int y)
    {
        if (x < 0 || y < 0 || width <= x || height <= y)
            return null;
        return nodes[y * width + x];
    }

    /**
     * This is meant to be used for drawing (if necessary).
     */
    public Node[] getNodes()
    {
        final Node[] nodes = new Node[this.nodes.length];

        for (int i = 0; i < this.nodes.length; ++i)
        {
            nodes[i] = this.nodes[i];
        }

        return nodes;
    }

    /**
     * Applies a path to the field.
     * 
     * @param path
     * @throws IllegalArgumentException if path is invalid for any reason (e.g. doesn't connect to the
     *                                  current node, uses already used edges)
     */
    public void applyPath(final Path path)
    {
        final ArrayList<PathElement> moves = path.getMoves();

        for (PathElement move : moves)
        {
            final Edge edge = current.getEdge(move.getMoveDirection());

            if (null == edge)
                throw new IllegalArgumentException("Edge doesn't exists.");
            if (edge.isBlocked())
                throw new IllegalArgumentException("Edge already visited.");

            edge.setVisitedBy(move.getPlayerId());

            final Node other = edge.getOpposite(current);
            other.setVisited(true);

            current = other;
        }

        this.path.addPath(path);
    }

    public Path getPath()
    {
        return path;
    }

    public void printFiled()
    {
        StringBuilder sb = new StringBuilder("\n");

        for (int y = height - 1; y >= 0; y--)
        {
            for (int x = 0; x < width; x++)
            {
                Node n = getNode(x, y);
                if (n == null)
                {
                    sb.append('n');
                }
                else if (n.isBorder())
                    sb.append('b');
                else if (n.isGoal())
                {
                    sb.append('g');
                }
                else if (n.isVisited())
                {
                    sb.append('v');
                }
                else
                {
                    // not visited
                    sb.append('o');
                }
            }
            sb.append("\n");
        }
        Log.d(LOG_TAG, sb.toString());
    }
}
