class Util {
    static guid() {
        return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    }
    static capitalize(s) {
        return s && s[0].toUpperCase() + s.slice(1);
    }
}

export default Util;

