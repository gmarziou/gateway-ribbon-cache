(function () {
    'use strict';

    angular
        .module('ribbonCacheApp')
        .factory('Register', Register);

    Register.$inject = ['$resource'];

    function Register ($resource) {
        return $resource('api/register', {}, {});
    }
})();
