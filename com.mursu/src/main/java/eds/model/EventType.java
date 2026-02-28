package eds.model;

import eds.framework.IEventType;

// DONE (by Ks, this file needs to be here, you can read the comment below)

/**
  EventType määrittelee simulaation mahdolliset tapahtumatyypit.

  Tämä enum toteuttaa IEventType-rajapinnan, koska
  simulaation framework (Event ja EventList) ei saa
  riippua suoraan tästä konkreettisesta enumista.

  Framework käsittelee tapahtumia yleisesti IEventType-tyyppinä.
  Malli (eli tämä enum) määrittelee varsinaiset tapahtumat.

  Näin erotetaan simulaation moottori ja
  pörssin varsinainen malli toisistaan.
 */

public enum EventType implements IEventType {
    ARRIVAL,
    VALIDATION_COMPLETE,
    MARKET_MATCHING_COMPLETE,
    LIMIT_MATCHING_COMPLETE,
    EXECUTION_COMPLETE
}
