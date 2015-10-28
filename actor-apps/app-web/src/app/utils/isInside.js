/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

const isInside = (coords, rect) => {
  return (
    coords.x > rect.left &&
    coords.y > rect.top &&
    coords.x < rect.left + rect.width &&
    coords.y < rect.top + rect.height
  )
};

export default isInside;
