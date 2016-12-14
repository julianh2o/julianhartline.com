var _ = require("lodash");
var fetch = require('node-fetch');

class CardService {
    static cardById(id) {
        return fetch("https://api.magicthegathering.io:443/v1/cards/"+id).then((res) => res.json()).then(function(result) {
            return result.card;
        });
    }
    static findCard(name) {
        return fetch("https://api.magicthegathering.io:443/v1/cards?name="+name).then((res) => res.json()).then(function(result) {
            var cards = result.cards;
            var firstCard = _.filter(cards,"imageUrl")[0];
            return firstCard;
        });
    }
    static generateBooster(set) {
        return fetch("https://api.magicthegathering.io:443/v1/sets/"+set+"/booster").then((res) => res.json()).then(function(result) {
            var cards = result.cards;
            return cards;
        });
    }
}

export default CardService;


