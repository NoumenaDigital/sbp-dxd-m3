/**
 * @name createCookie
 * @description
 * Creates a cookie with the provided name and value
 *
 * @param {String} name
 * @param {String} value
 * @param {Number} days
 */
export const createCookie = (
  name: string,
  value: string | undefined,
  days?: number,
): string => {
  let expires = '';
  if (days) {
    const date = new Date();
    date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000);
    expires = '; expires=' + date.toUTCString();
  }
  window.document.cookie = name + '=' + value + expires + '; path=/';
  const val = value ?? '';
  return val;
};

/**
 * @name readCookie
 * @description
 * Reads a cookie by it's name
 *
 * @param {String} name The name of the cookie
 * @returns {String} Will return an empty string when no cookie is present
 */
export const readCookie = (name: string): string => {
  const nameEQ = name + '=';
  const ca = window.document.cookie.split(';');
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) === ' ') c = c.substring(1, c.length);
    if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
  }
  return '';
};

/**
 * @name eraseCookie
 * @description
 * Erases a given cookie by it's name
 *
 * @param {String} name The name of the cookie
 * @returns {void}
 */
export const eraseCookie = (name: string): string => {
  return createCookie(name, '', -1);
};

/**
 * @name verifyCookie
 * @description
 * Checks if a cookie with a given name exists.
 *
 * @param {String} name The name of the cookie
 * @returns {Boolean} Returns true if the cookie exists false otherwise
 */
export const verifyCookie = (name: string): boolean => {
  var cks = window.document.cookie.split(';');
  if (cks.length < 1) return false;
  for (const item of cks) if (item.split('=')[0].trim() == name) return true;
  return false;
};

export function useCookieRepo(key: string) {
  const cookieKey = key;
  return {
    add: (next: string) => createCookie(cookieKey, next),
    read: () => readCookie(cookieKey),
    remove: () => eraseCookie(cookieKey),
    verify: () => eraseCookie(cookieKey),
  };
}
