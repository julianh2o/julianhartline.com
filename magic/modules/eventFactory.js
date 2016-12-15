import Util from "./util";

class EventFactory {
    static updateObject(o) {
        return {
            type: "update",
            id: o.id,
            top: o.top,
            left: o.left,
        }
    }
    static createCollection(objects) {
        return {
            type: "createCollection",
            id: Util.guid(),
            cards: _.map(objects,"id"),
            top: objects[0].top,
            left: objects[0].left,
        }
    }
    static dismantleCollection(o) {
        return {
            type: "dismantleCollection",
            id: o.id,
        }
    }
    static removeFromCollection(o) {
        return {
            type: "removeFromCollection",
            id: o.id,
        }
    }
    static addToCollection(collection,cardOrCards,index) {
        return {
            type: "addToCollection",
            id: collection.id,
            index: index === undefined ? -1 : index,
            cards: _.map(Array.isArray(cardOrCards) ? cardOrCards : [cardOrCards],"id"),
        }
    }
}

export default EventFactory;


