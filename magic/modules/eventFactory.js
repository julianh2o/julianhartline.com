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
            collectionId: o.group.id,
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
    static mergeIntoCollection(objects) {
        return {
            type: "mergeIntoCollection",
            id: Util.guid(),
            objects: _.map(objects,"id"),
        }
    }
    static spawnBooster(x,y,set) {
        return {
            type: "spawnBooster",
            x: x,
            y: y,
            set: set,
            broadcast: false,
        }
    }
    static objectsCreated(objects) {
        return {
            type: "objectsCreated",
            objects: objects,
        }
    }
    static openBooster(o) {
        return {
            type: "openBooster",
            id: o.id,
        }
    }
}

export default EventFactory;


