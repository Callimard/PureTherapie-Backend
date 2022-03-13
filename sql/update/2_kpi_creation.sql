INSERT INTO puretherapie.KPI (name, description, tags)
VALUES ('ClientPurchasesKPI', 'Informations sur les achats de produits avec les taux d\'achat des nouveaux client', ';client;purchase;ac;package;'),
       ('FillingRateKPI', 'Taux de remplissage en terme de rendez-vous', ';rate;appointment;'),
       ('NewClientAndOriginKPI', 'Informations sur les renz-vous des nouveaux clients et comment ils ont connus PureTherapie',
        ';appointment;client;origin;'),
       ('NotFinalizedAppointmentKPI', 'Informations sur tous les rendez-vous non finalizé mais pour qui un soin a été pratique.',
        ';appointment;finalization;client;'),
       ('SurbookingKPI', 'Information sur le nombre de surbooking créé', ';surbooking;appointment;'),
       ('TechnicianACProvisionKPI', 'Informations sur les soins partiqués par les techniciennes et le total du prix des soins pratiqués.',
        ';technician;ac;price;'),
       ('TurnoverKPI', 'Chiffre d\'affaire totale + chiffre d\'affaire pour chaque moyens de paiments', ';turnover;payment;');
