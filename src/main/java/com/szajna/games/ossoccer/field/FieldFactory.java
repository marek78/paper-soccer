package com.szajna.games.ossoccer.field;

public class FieldFactory
{
    public static Field createStandard()
    {
        return create(9, 13);
    }

    public static Field create(final int width, final int height)
    {
        if (width < 0 || height < 0)
            throw new IllegalArgumentException("Width and height must be bigger than zero.");
        if (0 == (width % 2) || 0 == (height % 2))
            throw new IllegalArgumentException("Only odd values are accepted as width and height.");

        final Node[] nodes = new Node[width * height];

        final int gateXMin = width / 2 - 1;
        final int gateXMax = width / 2 + 1;

        // Creating nodes.
        boolean border;
        {
            for (int j = 1; j < (height - 1); ++j)
            {
                for (int i = 0; i < width; ++i)
                {
                    border = 0 == i || (width - 1) == i;

                    if (!border && (1 == j || (height - 2) == j))
                    {
                        border = i <= gateXMin || gateXMax <= i;
                    }

                    nodes[j * width + i] = new Node(i, j, border, false);
                }
            }

            for (int i = -1; i < 2; ++i)
            {
                nodes[i + (width / 2)] = new Node(i + (width / 2), 0, true, true);
                nodes[((height - 1) * width) + i + (width / 2)] = new Node(i + (width / 2), height - 1, true, true);
            }
        }

        final Node center = nodes[width / 2 + (height / 2) * width];
        // center is always visited
        center.setVisited(true);
        final Field field = new Field(width, height, nodes, center);

        // Creating edges.
        {
            for (int j = 0; j < height; ++j)
            {
                for (int i = 0; i < width; ++i)
                {
                    final Node node = field.getNode(i, j);

                    if (null == node)
                        continue;

                    setEdge(node, 0, i, j + 1, field);
                    setEdge(node, 1, i + 1, j + 1, field);
                    setEdge(node, 2, i + 1, j, field);
                    setEdge(node, 3, i + 1, j - 1, field);
                    setEdge(node, 4, i, j - 1, field);
                    setEdge(node, 5, i - 1, j - 1, field);
                    setEdge(node, 6, i - 1, j, field);
                    setEdge(node, 7, i - 1, j + 1, field);
                }
            }
        }

        return field;
    }

    private static void setEdge(final Node node, final int dir, final int ox, final int oy, final Field field)
    {
        if (dir < 0 || 8 <= dir)
            throw new IllegalArgumentException("Bad direction value.");

        if (null != node.getEdge(dir))
            return;

        final Node other = field.getNode(ox, oy);

        if (null == other)
            return;

        {
            final int x1 = field.getWidth() / 2 - 1;
            final int x2 = field.getWidth() / 2 + 1;
            final int y1 = field.getHeight() - 1;
            final int y2 = field.getHeight() - 2;

            if ((0 == node.getY() && 1 == other.getY()) || (y1 == node.getY() && y2 == other.getY()))
            {
                if (x1 == node.getX() && (x1 - 1) == other.getX())
                    return;
                if (x2 == node.getX() && (x2 + 1) == other.getX())
                    return;
            }

            if ((0 == other.getY() && 1 == node.getY()) || (y1 == other.getY() && y2 == node.getY()))
            {
                if (x1 == other.getX() && (x1 - 1) == node.getX())
                    return;
                if (x2 == other.getX() && (x2 + 1) == node.getX())
                    return;
            }
        }

        final boolean blocked = node.isBorder() && other.isBorder()
                && (node.getX() == other.getX() || node.getY() == other.getY());
        final Edge edge = new Edge(node, other, blocked);
        node.setEdge(edge, dir);
        other.setEdge(edge, (dir + 4) % 8);
    }
}
