/*global app, Router */

(function (app, Router) {

    'use strict';

    var router = new Router();

    ['all', 'active', 'completed'].forEach(function (visibility) {
        router.on(visibility, function () {
            // a trick way to update property defined in Vue.js 3 component
            // reference: https://stackoverflow.com/questions/68632488/how-do-i-access-vue-instance-inside-a-js-file-in-vue3 & https://jsfiddle.net/9hc6rza2/
            app._instance.ctx.$data.visibility = visibility;
        });
    });

    router.configure({
        notfound: function () {
            window.location.hash = '';
            app._instance.ctx.$data.visibility = 'all';
        }
    });

    router.init();

})(app, Router);
