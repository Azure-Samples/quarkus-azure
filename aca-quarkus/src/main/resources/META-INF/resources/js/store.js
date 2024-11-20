/*jshint unused:false */

(function (exports) {

    'use strict';

    var serverUrl = 'api/';

    exports.todoStorage = {
        fetch: async function () {
            const response = await fetch(serverUrl);
            const data = await response.json();
            console.log(data);
            return data;
        },
        add : async function(item) {
          console.log("Adding todo item " + item.title);
          const response = await fetch(serverUrl, {
              method: 'POST',
              headers: {
                  'Content-Type': 'application/json'
              },
              body: JSON.stringify(item)
          });
          return await response.json();
        },
        save: async function (item) {
            console.log("save called with", item);
            await fetch(serverUrl + item.id, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(item)
            });
        },
        delete: async function(item) {
            console.log("delete called with", item);
            await fetch(serverUrl + item.id, {
                method: 'DELETE'
            });
        },
        deleteCompleted: async function() {
            console.log("deleteCompleted called");
            await fetch(serverUrl, {
                method: 'DELETE'
            });
        }
    };

})(window);
