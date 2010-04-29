    create table currency (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
    );

    create table commodityfuturetype (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
    );

    create table bondfuturetype (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
    );

    create table cashrate (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
    );

    create table unit (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
    );

    create table domain_specific_identifier_association (
        id bigint not null,
        security_discriminator varchar(255),
        security_id bigint,
        domain varchar(255) not null,
        identifier varchar(255) not null,
        validStartDate date,
        validEndDate date,
        primary key (id),
        unique (domain, identifier, validStartDate, validEndDate)
    );

    create table exchange (
        id bigint not null,
        name varchar(255) not null unique,
        description varchar(255),
        primary key (id)
    );

    create table gics (
      id bigint not null,
      name varchar(8) not null unique,
      description varchar(255),
      primary key (id)
    );

    create table equity (
        id bigint not null,
        effectiveDateTime date not null,
        deleted smallint not null,
        lastModifiedDateTime date not null,
        lastModifiedBy varchar(255),
        displayName varchar(255) not null,
        first_version_descriminator varchar(255),
        first_version_id bigint,
        exchange_id bigint not null,
        companyName varchar(255) not null,
        currency_id bigint not null,
        gicscode_id bigint,
        primary key (id),
       constraint fk_equity2currency foreign key (currency_id) references currency(id),
       constraint fk_equity2exchange foreign key (exchange_id) references exchange(id),
        constraint fk_equity2gics foreign key (gicscode_id) references gics(id)
    );
    
    create table optionsec (
        id bigint not null,
        effectiveDateTime date not null,
        deleted smallint not null,
        lastModifiedDateTime date not null,
        lastModifiedBy varchar(255),
        displayName varchar(255) not null,
        first_version_descriminator varchar(255),
        first_version_id bigint,
        option_security_type varchar(32) not null,
        option_type varchar(32) not null,
        strike double precision not null,
        expiry date not null,
        underlyingIdentityKey varchar(255),
        currency1_id bigint not null,
        currency2_id bigint,
        currency3_id bigint,
        exchange_id bigint,
        counterparty varchar(255),
        power double,
        margined smallint,
        primary key (id),
        constraint fk_option2currency1 foreign key (currency1_id) references currency (id),
        constraint fk_option2currency2 foreign key (currency2_id) references currency (id),
        constraint fk_option2currency3 foreign key (currency3_id) references currency (id),
        constraint fk_option2exchange foreign key (exchange_id) references exchange (id)
    );
    
    create table frequency (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
    );
    
    create table daycount (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
    );
    
    create table businessdayconvention (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
    );
    
    create table issuertype (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
     );
    
    create table market (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
     );
    
    create table yieldconvention (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
     );
    
    create table guaranteetype (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
     );
    
    create table coupontype (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
     );
    
    create table bond (
        id bigint not null,
        effectiveDateTime date not null,
        deleted smallint not null,
        lastModifiedDateTime date not null,
        lastModifiedBy varchar(255),
        displayName varchar(255) not null,
        first_version_descriminator varchar(255),
        first_version_id bigint,
        bond_type varchar(32) not null,
        issuername varchar(255) not null,
        issuertype_id bigint not null,
        issuerdomicile varchar(255) not null,
        market_id bigint not null,
        currency_id bigint not null,
        yieldconvention_id bigint not null,
        guaranteetype_id bigint not null,
        maturity date not null,
        coupontype_id bigint not null,
        couponrate double not null,
        couponfrequency_id bigint not null,
        daycountconvention_id bigint not null,
        businessdayconvention_id bigint not null,
        announcementdate date not null,
        interestaccrualdate date not null,
        settlementdate date not null,
        firstcoupondate date not null,
        issuanceprice double not null,
        totalamountissued double not null,
        minimumamount double not null,
        minimumincrement double not null,
        paramount double not null,
        redemptionvalue double not null,
        primary key (id),
        constraint fk_bond2issuertype foreign key (issuertype_id) references issuertype (id),
        constraint fk_bond2market foreign key (market_id) references market (id),
        constraint fk_bond2currency foreign key (currency_id) references currency (id),
        constraint fk_bond2yieldconvention foreign key (yieldconvention_id) references yieldconvention (id),
        constraint fk_bond2guaranteetype foreign key (guaranteetype_id) references guaranteetype (id),
        constraint fk_bond2coupontype foreign key (coupontype_id) references coupontype (id),
        constraint fk_bond2frequency foreign key (couponfrequency_id) references frequency (id),
        constraint fk_bond2daycount foreign key (daycountconvention_id) references daycount (id),
        constraint fk_bond2businessdayconvention foreign key (businessdayconvention_id) references businessdayconvention (id)
    );

    create table future (
        id bigint not null,
        effectiveDateTime date not null,
        deleted smallint not null,
        lastModifiedDateTime date not null,
        lastModifiedBy varchar(255),
        displayName varchar(255) not null,
        first_version_descriminator varchar(255),
        first_version_id bigint,
        future_type varchar(32) not null,
        expiry date not null,
        tradingexchange_id bigint not null,
        settlementexchange_id bigint not null,
        currency1_id bigint,
        currency2_id bigint,
        currency3_id bigint,
        bondtype_id bigint,
        commoditytype_id bigint,
        cashratetype_id bigint,
        unitname_id bigint,
        unitnumber double precision,
        primary key (id),
        constraint fk_future2exchange1 foreign key (tradingexchange_id) references exchange (id),
        constraint fk_future2exchange2 foreign key (settlementexchange_id) references exchange (id),
        constraint fk_future2currency1 foreign key (currency1_id) references currency (id),
        constraint fk_future2currency2 foreign key (currency2_id) references currency (id),
        constraint fk_future2currency3 foreign key (currency3_id) references currency (id),
        constraint fk_future2bondfuturetype foreign key (bondtype_id) references bondfuturetype (id),
        constraint fk_future2commodityfuturetype foreign key (commoditytype_id) references commodityfuturetype (id),
        constraint fk_future2cashrate foreign key (cashratetype_id) references cashrate (id),
        constraint fk_future2unit foreign key (unitname_id) references unit (id)
    );
    
    create table future_basket (
        id bigint not null,
        future_id bigint not null,
        domain varchar(255) not null,
        identifier varchar(255) not null,
        primary key (id),
        constraint fk_future_basket2future foreign key (future_id) references future (id),
        unique (future_id, domain, identifier)
    );
