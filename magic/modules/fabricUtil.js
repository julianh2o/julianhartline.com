class FabricUtil {
    static getAbsoluteCenterPoint(o) {
      var point = o.getCenterPoint();
      if (!o.group)
        return point;
      var groupPoint = Util.getAbsoluteCenterPoint(o.group);
      return {
        x: point.x + groupPoint.x,
        y: point.y + groupPoint.y
      };
    }
    static containsInGroupPoint(o,point) {
      if (!o.group)
        return o.containsPoint(point);

      var center = Util.getAbsoluteCenterPoint(o);
      var thisPos = {
          xStart: center.x - o.width/2,
          xEnd: center.x + o.width/2,
          yStart: center.y - o.height/2,
          yEnd: center.y + o.height/2
      }

      if (point.x >= thisPos.xStart && point.x <= (thisPos.xEnd)) {
          if (point.y >= thisPos.yStart && point.y <= thisPos.yEnd) {
              return true;
          }
      }
      return false;
    }
    static objectAt(canvas,x,y,exclude) {
        var point = new fabric.Point(x,y);
        if (!$.isArray(exclude)) exclude = [exclude];
        var found = null;
        _.each(canvas.getObjects(),function(o) {
            if (!_.includes(exclude,o) && o.containsPoint(point)) {
                found = o;
            }
        });
        return found;
    }
    static hideRotationAndScalingControls(o) {
        o.hasControls = false;
        o.lockRotation = true;
        o.lockScalingX = true;
        o.lockScalingY = true;
    }
}

export default FabricUtil;


