package com.tortel.deploytrack.data;

import androidx.room.Embedded;
import androidx.room.Relation;

public class WidgetInfoAndDeployment {
    @Embedded public Deployment deployment;
    @Relation(
            parentColumn = "deploymentId",
            entityColumn = "uuid"
    )
    public WidgetInfo widgetInfo;
}
