package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("redundancy")
public class RedundancyFilter implements BlueprintsFilter {

    @Override
    public Blueprint apply(Blueprint bp) {
        List<Point> originalPoints = bp.getPoints();
        List<Point> filteredPoints = new ArrayList<>();

        if (originalPoints.isEmpty()) {
            return new Blueprint(bp.getAuthor(), bp.getName(), filteredPoints);
        }

        Point previous = originalPoints.get(0);
        filteredPoints.add(previous);

        for (int i = 1; i < originalPoints.size(); i++) {
            Point current = originalPoints.get(i);
            if (!current.equals(previous)) {
                filteredPoints.add(current);
                previous = current;
            }
        }

        return new Blueprint(bp.getAuthor(), bp.getName(), filteredPoints);
    }
}