
DOMAIN
    LOOP
        Start Loop              (done)
        End Loop                (done)
        ON STARTUP
            Create Users        (done)
            Create Decks        (done)
            Create Cards        (done)
            Create Collabs      (done)
            Accept Collabs      (done)
        RANDOM BEHAVIOR
            Review Card

WEB
    USER-Service
        Create User             (done)
    DECK-Service
        Create Deck             (done)
        Create Card             (done)
        Override Card           (done)
        Review Card             (done)
    COLLAB-Service
        Start Collaboration     (done)
        Accept Collaboration    (done)
        End Collaboration       (done)

KAFKA
    -


# race condition create deck when user created.
