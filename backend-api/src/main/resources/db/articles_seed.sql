-- Rich, original MamaCarePlus pregnancy articles.
-- Safe to run multiple times: matching titles are updated, missing titles are inserted.

DROP TABLE IF EXISTS mama_article_seed;

CREATE TEMP TABLE mama_article_seed (
    title TEXT,
    category TEXT,
    content TEXT,
    image_url TEXT,
    reading_time_minutes INTEGER,
    video_url TEXT,
    video_title TEXT
);

INSERT INTO mama_article_seed (title, category, content, image_url, reading_time_minutes, video_url, video_title) VALUES
(
    'Construire une assiette équilibrée pendant la grossesse',
    'Nutrition',
    $article$Pendant la grossesse, une assiette équilibrée n’a pas besoin d’être compliquée. Le plus utile est de penser en familles d’aliments: une source de protéines, une portion de féculents, des légumes ou des fruits, un produit laitier ou une alternative adaptée, et une boisson simple comme l’eau. Cette organisation aide à garder de l’énergie, à limiter les grignotages liés aux grosses faims et à couvrir progressivement les besoins de la journée.

Les protéines peuvent venir des œufs bien cuits, du poisson autorisé, de la viande bien cuite, des légumineuses, du yaourt ou du fromage pasteurisé. Les féculents complets, comme le pain complet, le riz complet ou les flocons d’avoine, apportent une énergie plus régulière. Les légumes cuits sont souvent mieux tolérés si vous avez des nausées ou des ballonnements. Les fruits peuvent être pris au dessert ou en collation, surtout lorsque l’appétit change selon les moments.

Il est préférable d’éviter les règles trop strictes. Certaines journées seront plus faciles que d’autres. Si vous êtes fatiguée, un repas simple peut rester correct: une soupe de légumes, un œuf bien cuit, un morceau de pain et un yaourt, par exemple. Pensez aussi à la sécurité alimentaire: laver les fruits et légumes, bien cuire les viandes et poissons, respecter la chaîne du froid, et éviter les produits non pasteurisés quand ils sont déconseillés.

Cette information reste générale. Si vous avez du diabète gestationnel, des vomissements importants, une perte de poids, une anémie ou un régime particulier, demandez un avis personnalisé à votre médecin, sage-femme ou diététicienne. L’objectif n’est pas de manger parfaitement, mais de construire des repères rassurants et réguliers.$article$,
    'local://article_nutrition_assiette',
    5,
    NULL,
    NULL
),
(
    'Fer, folates et calcium sans se compliquer',
    'Nutrition',
    $article$Fer, folates et calcium reviennent souvent dans les discussions de grossesse, car ils participent au bon fonctionnement du corps maternel et au développement du bébé. Il n’est pas nécessaire de tout mémoriser comme un cours de biologie. Le plus important est de savoir où les trouver dans l’alimentation, pourquoi le suivi médical les surveille, et quand un complément peut être proposé.

Les folates, aussi appelés vitamine B9, sont particulièrement importants en début de grossesse. On les retrouve dans les légumes verts, les légumineuses, certains fruits, les œufs et les produits céréaliers enrichis selon les pays. Le fer aide au transport de l’oxygène. Il est présent dans les viandes bien cuites, les poissons autorisés, les lentilles, les pois chiches, les haricots et certains fruits secs. Associer une source de vitamine C, comme un agrume ou un fruit frais, peut aider l’absorption du fer végétal.

Le calcium contribue à la santé des os et des dents. Les produits laitiers pasteurisés, certains yaourts, fromages autorisés, boissons enrichies et légumes verts peuvent y participer. L’hydratation est aussi utile, surtout si vous augmentez les fibres. Essayez de répartir ces aliments sur la semaine plutôt que de chercher une assiette parfaite chaque jour.

Les compléments ne doivent pas être pris au hasard. Certaines femmes ont besoin de fer, d’acide folique, de vitamine D ou d’iode selon leur situation, leurs analyses et leurs habitudes alimentaires. D’autres n’en ont pas besoin au même dosage. Si vous êtes végétarienne, si vous avez une grande fatigue, des vertiges, une pâleur, ou si vos analyses montrent une carence, parlez-en à votre professionnel de santé. Un conseil individualisé évite les manques mais aussi les excès inutiles.$article$,
    'local://article_nutrition_micronutrients',
    5,
    NULL,
    NULL
),
(
    'Nausées et petits repas adaptés',
    'Nutrition',
    $article$Les nausées sont fréquentes pendant la grossesse, surtout au premier trimestre, mais leur intensité varie beaucoup d’une femme à l’autre. Elles peuvent apparaître le matin, le soir, après certaines odeurs, ou quand l’estomac reste vide trop longtemps. L’objectif n’est pas de se forcer à manger un grand repas, mais de trouver des petites prises alimentaires mieux tolérées et suffisamment régulières pour garder de l’énergie.

Beaucoup de femmes se sentent mieux avec des repas fractionnés. Vous pouvez essayer une petite collation avant de vous lever, comme un biscuit sec, un morceau de pain grillé ou quelques crackers. Dans la journée, les aliments simples, peu gras et peu épicés sont souvent plus faciles: riz, pommes de terre, pâtes, compote, banane, soupe douce, yaourt pasteurisé ou œuf bien cuit. Les repas tièdes ou froids peuvent aussi être mieux acceptés car ils dégagent moins d’odeurs.

Boire reste important, mais certaines femmes tolèrent mal de grandes quantités d’un coup. Essayez de petites gorgées régulières, de l’eau fraîche, une eau aromatisée maison avec citron si vous le supportez, ou une tisane autorisée. Évitez de culpabiliser si votre alimentation est moins variée pendant quelques jours: l’essentiel est de rester hydratée et de reprendre progressivement une diversité alimentaire quand les nausées diminuent.

Certains signes demandent un avis médical: vomissements répétés, impossibilité de garder les liquides, perte de poids, urines très foncées, vertiges importants ou grande faiblesse. Votre médecin ou sage-femme peut proposer des solutions adaptées et vérifier qu’il n’y a pas de déshydratation. Ne prenez pas de médicament anti-nausée sans avis professionnel, même s’il vous a été conseillé par quelqu’un d’autre.$article$,
    '/article-images/article_nutrition_nausees.jfif',
    4,
    NULL,
    NULL
),
(
    'Hydratation pendant la grossesse',
    'Nutrition',
    $article$L’hydratation est un repère simple mais important pendant la grossesse. Le volume sanguin augmente, la digestion peut ralentir, la température corporelle peut sembler plus sensible et les besoins varient selon la chaleur, l’activité et les nausées. Boire régulièrement aide à soutenir le confort général, mais cela ne veut pas dire qu’il faut se forcer à avaler de grandes quantités d’un coup. Le plus efficace est souvent de répartir les boissons sur la journée.

Gardez une bouteille près de vous et buvez par petites gorgées. Si l’eau nature vous dégoûte, essayez une eau fraîche, une rondelle de citron bien lavé, quelques feuilles de menthe, ou une boisson chaude douce autorisée. Les soupes, fruits riches en eau, compotes sans excès de sucre et légumes cuits peuvent aussi participer aux apports. Les boissons très sucrées ou très caféinées ne doivent pas devenir la base de l’hydratation. Si vous avez une restriction médicale, par exemple liée à une maladie rénale ou cardiaque, suivez les consignes de votre médecin.

Certaines situations demandent plus d’attention: chaleur, fièvre, diarrhée, vomissements, activité physique ou longs trajets. Des urines très foncées, une bouche sèche, des vertiges, une grande faiblesse ou l’impossibilité de garder les liquides peuvent faire penser à une déshydratation. Dans ce cas, contactez rapidement un professionnel de santé, surtout si les vomissements sont répétés.

L’hydratation peut aussi aider indirectement la constipation, fréquente pendant la grossesse, surtout si vous augmentez les fibres. Avancez progressivement: plus de fibres sans assez d’eau peut parfois aggraver les ballonnements. Comme toujours, ces conseils restent généraux. Votre sage-femme ou médecin peut vous donner des repères adaptés si vous avez des contractions, un diabète gestationnel, des œdèmes importants ou un autre suivi particulier.$article$,
    'local://article_hydration',
    4,
    NULL,
    NULL
),
(
    'Les signes à surveiller sans paniquer',
    'Santé',
    $article$La grossesse s’accompagne de nombreuses sensations nouvelles: tiraillements, fatigue, changements digestifs, douleurs légères, essoufflement modéré ou contractions ponctuelles en fin de grossesse. Beaucoup de ces signes peuvent être bénins, mais il est utile de connaître ceux qui méritent une réaction rapide. Savoir quoi surveiller permet de rester attentive sans vivre chaque symptôme comme une urgence.

Demandez rapidement un avis médical en cas de saignement, perte de liquide, fièvre, douleur abdominale forte ou persistante, maux de tête intenses, troubles de la vision, gonflement brutal du visage ou des mains, douleur dans la poitrine, difficulté à respirer, malaise, brûlures urinaires importantes, ou contractions régulières avant le terme. Après la période où vous sentez habituellement le bébé bouger, une diminution nette ou inhabituelle des mouvements doit aussi conduire à contacter la maternité ou votre professionnel de santé.

Il est important de décrire simplement ce que vous ressentez: depuis quand, à quel endroit, avec quelle intensité, ce qui l’aggrave ou le soulage, et s’il existe d’autres signes associés. Gardez vos documents de suivi accessibles et notez les numéros utiles: sage-femme, maternité, médecin, urgences. En cas de doute, il vaut mieux appeler que rester seule avec une inquiétude.

Cette liste ne permet pas de poser un diagnostic et ne remplace pas une consultation. Elle sert de repère de sécurité. Chaque grossesse est différente, notamment si vous avez des antécédents médicaux, une grossesse multiple, une tension élevée, un diabète gestationnel ou un risque particulier déjà identifié. Votre équipe de suivi est la meilleure référence pour adapter les consignes à votre situation.$article$,
    '/article-images/article_sante_signes.jpg',
    5,
    '/videos/Breathing%20exercise%20for%20stress%20and%20anxiety.mp4',
    'Respiration douce contre le stress'
),
(
    'Sommeil, fatigue et récupération',
    'Santé',
    $article$La fatigue pendant la grossesse peut surprendre par son intensité. Au premier trimestre, les changements hormonaux et les adaptations du corps peuvent donner envie de dormir plus que d’habitude. Plus tard, le volume du ventre, les envies d’uriner, les mouvements du bébé, les remontées acides ou les douleurs du dos peuvent rendre les nuits moins réparatrices. Le sommeil devient alors un vrai sujet de bien-être, pas un détail.

Essayez de créer une routine simple: dîner léger si les reflux sont présents, réduire les écrans avant de dormir, préparer les affaires du lendemain pour diminuer la charge mentale, et garder une chambre aussi calme que possible. Une position sur le côté, avec un coussin entre les genoux ou sous le ventre, peut soulager certaines tensions. Si vous travaillez ou avez d’autres enfants, de petites pauses de dix à vingt minutes peuvent aider, même sans dormir profondément.

La récupération passe aussi par l’organisation. Accepter de ralentir, déléguer certaines tâches et simplifier les repas n’est pas un échec. Le corps dépense de l’énergie pour la grossesse. Une marche douce, quelques étirements autorisés ou une respiration calme peuvent parfois améliorer l’endormissement, à condition de ne pas forcer.

Parlez de votre fatigue si elle devient extrême, si elle s’accompagne d’essoufflement important, de palpitations, de vertiges, de tristesse persistante ou d’une incapacité à fonctionner au quotidien. Une anémie, un trouble thyroïdien, une tension anormale ou une souffrance psychologique peuvent nécessiter un suivi. Se reposer est utile, mais être écoutée l’est aussi. Votre médecin ou sage-femme peut vous aider à faire la différence entre fatigue habituelle et signe à explorer.$article$,
    '/article-images/article_sante_sommeil.jpg',
    4,
    NULL,
    NULL
),
(
    'Comprendre le suivi du poids',
    'Santé',
    $article$Le poids pendant la grossesse est un indicateur de suivi, mais il ne résume pas votre santé ni la qualité de votre grossesse. Il varie selon le poids de départ, la taille, les nausées, la rétention d’eau, l’activité, l’alimentation, la croissance du bébé, le liquide amniotique et le placenta. Certaines femmes prennent rapidement au début, d’autres très peu à cause des nausées, puis davantage ensuite. Le plus important est la tendance globale et le contexte médical.

Se peser une fois par semaine, dans des conditions similaires, peut aider à observer l’évolution sans se focaliser sur les variations quotidiennes. Le matin, après être allée aux toilettes et avec des vêtements légers, donne souvent une mesure plus comparable. Dans l’application, noter le poids chaque semaine permet de visualiser la courbe et de repérer les changements brusques. Une courbe sert à discuter, pas à juger.

Une prise de poids très rapide, surtout avec gonflement important, maux de tête ou troubles visuels, doit être signalée. Une perte de poids persistante, des vomissements importants ou une difficulté à manger doivent aussi être abordés. À l’inverse, si votre poids augmente plus que prévu, évitez les régimes restrictifs sans accompagnement. Pendant la grossesse, l’objectif est d’ajuster les habitudes de manière sûre, pas de chercher une perte de poids.

Votre professionnel de santé peut vous donner une fourchette adaptée selon votre situation. Les recommandations générales ne remplacent pas ce conseil personnalisé. Si vous avez un diabète gestationnel, une hypertension, une grossesse multiple ou un antécédent médical, le suivi peut être différent. Utilisez le poids comme un repère parmi d’autres: tension, analyses, croissance du bébé, symptômes et ressenti global.$article$,
    '/article-images/article_sante_poids.jpg',
    5,
    NULL,
    NULL
),
(
    'Bébé grandit semaine après semaine',
    'Bébé',
    $article$Suivre le développement du bébé semaine après semaine aide à rendre la grossesse plus concrète. Au début, les changements sont minuscules mais très importants: les premières structures se mettent en place, puis les organes poursuivent leur maturation. Ensuite, le bébé grandit, prend du poids, bouge davantage et réagit de plus en plus à son environnement. Les comparaisons avec des fruits ou des objets peuvent aider à imaginer sa taille, mais elles restent des repères approximatifs.

Chaque grossesse a son rythme. Les mesures données dans les applications sont des moyennes. Elles ne remplacent pas les échographies ni l’évaluation de votre sage-femme ou médecin. Une estimation de poids peut varier selon la méthode, la position du bébé et le terme exact. Il est donc normal que les chiffres ne correspondent pas parfaitement à ce que vous lisez dans un tableau. Ce qui compte, c’est l’évolution observée par l’équipe médicale.

Pour profiter de ce suivi sans anxiété, utilisez les informations comme un moment de connexion: lire la semaine actuelle, noter une question, observer vos sensations, parler au bébé si vous le souhaitez, ou partager l’étape avec le co-parent. Si une donnée vous inquiète, évitez de comparer plusieurs sources jusqu’à vous perdre. Notez la question et demandez-la au prochain rendez-vous.

Certains signes doivent être signalés, notamment une diminution nette des mouvements après la période où vous les sentez régulièrement, des douleurs importantes, des saignements ou une perte de liquide. Le développement du bébé est un sujet passionnant, mais il doit rester accompagné par un suivi médical. L’application donne des repères; votre équipe de soins donne l’interprétation adaptée à votre situation.$article$,
    '/article-images/article_bebe_developpement.jfif',
    5,
    '/videos/K0013980-Pregnant_woman_ultrasound.mp4',
    'Échographie pendant la grossesse'
),
(
    'Mouvements du bébé et moments calmes',
    'Bébé',
    $article$Les premiers mouvements du bébé sont souvent décrits comme des bulles, des petits battements ou un léger glissement. Ils apparaissent à des moments différents selon les femmes, la position du placenta, la corpulence, le nombre de grossesses précédentes et l’attention portée aux sensations. Pour une première grossesse, il est fréquent de les reconnaître plus tard que pour une grossesse suivante. Cela ne signifie pas forcément que quelque chose ne va pas.

Quand les mouvements deviennent plus nets, ils peuvent varier selon l’heure, votre activité, vos repas ou les périodes de sommeil du bébé. Beaucoup de femmes les sentent davantage au calme, en position assise ou allongée. Prendre quelques minutes pour respirer, poser les mains sur le ventre et observer peut devenir un rituel doux. Il ne s’agit pas de contrôler chaque mouvement, mais d’apprendre peu à peu le rythme habituel de votre bébé.

Si vous êtes très occupée, stressée ou en mouvement toute la journée, vous pouvez moins remarquer les sensations. Un moment calme peut aider à faire le point. Cependant, après la période où les mouvements sont habituellement réguliers, une diminution nette, inhabituelle ou inquiétante doit être prise au sérieux. Contactez votre maternité, sage-femme ou médecin selon les consignes reçues. N’attendez pas le lendemain si votre intuition vous alerte.

Évitez de vous fier uniquement aux conseils trouvés en ligne ou aux expériences d’autres femmes. Chaque grossesse est différente. Votre équipe de suivi peut vous expliquer à partir de quand surveiller les mouvements et comment réagir. Les moments calmes avec le bébé doivent rester rassurants, mais ils ne remplacent pas un avis professionnel lorsque quelque chose semble différent.$article$,
    '/article-images/article_bebe_mouvements.jpg',
    4,
    '/videos/K0013980-Pregnant_woman_ultrasound.mp4',
    'Échographie pendant la grossesse'
),
(
    'Bouger en douceur pendant la grossesse',
    'Activité',
    $article$L’activité physique douce peut soutenir le bien-être pendant la grossesse lorsqu’il n’existe pas de contre-indication médicale. Marcher, nager, faire du vélo d’appartement à intensité légère, pratiquer du yoga prénatal encadré ou des exercices de mobilité peut aider la circulation, le sommeil, l’humeur et certaines douleurs du dos. Le but n’est pas la performance, mais la régularité et l’écoute du corps.

Un bon repère est de pouvoir parler pendant l’effort. Si vous êtes essoufflée au point de ne plus pouvoir tenir une conversation, ralentissez. Commencez par de courtes durées si vous n’étiez pas active avant la grossesse: dix minutes de marche peuvent déjà être utiles. Portez des chaussures stables, hydratez-vous, évitez la chaleur excessive et choisissez des lieux où vous vous sentez en sécurité. Les mouvements avec risque de chute, de choc ou de pression importante sur le ventre doivent être évités.

Certaines sensations imposent d’arrêter et de demander conseil: saignement, douleur thoracique, vertige, perte de liquide, contractions douloureuses ou régulières, essoufflement inhabituel, douleur forte du bassin ou du mollet. Si votre grossesse est considérée à risque, si vous avez une hypertension, un placenta bas, une menace d’accouchement prématuré ou une autre situation médicale, l’activité doit être validée par votre professionnel de santé.

Bouger peut aussi être mental: sortir prendre l’air, étirer les épaules, respirer profondément. Les jours de fatigue, une séance très courte suffit. Les jours plus confortables, vous pouvez prolonger doucement. L’important est de construire une relation confiante avec votre corps, sans comparaison avec les autres femmes ni avec vos performances d’avant grossesse.$article$,
    '/article-images/article_activite_douce.jpg',
    5,
    '/videos/Entra%C3%AEnement%20pr%C3%A9natal%20%20_%20bouger%20durant%20la%20grossesse.mp4',
    'Entraînement prénatal doux'
),
(
    'Soulager le dos avec des gestes simples',
    'Activité',
    $article$Les douleurs du dos sont fréquentes pendant la grossesse. Le centre de gravité change, le ventre tire vers l’avant, les ligaments deviennent plus souples et la fatigue peut modifier la posture. Ces douleurs ne sont pas toujours graves, mais elles peuvent gêner le sommeil, la marche ou les activités quotidiennes. Quelques gestes simples peuvent aider à diminuer les tensions sans forcer.

Essayez d’abord d’observer les moments où la douleur augmente: station debout prolongée, port de charges, position assise, fin de journée, chaussures peu stables. Alterner les positions est souvent plus efficace que chercher une posture parfaite. En position assise, gardez les pieds au sol et soutenez le bas du dos avec un coussin. Pour ramasser un objet, pliez les genoux plutôt que de vous pencher brusquement. Pour dormir, un coussin entre les genoux peut améliorer l’alignement du bassin.

Des étirements doux peuvent être utiles si votre professionnel de santé les autorise: mouvements lents du bassin, ouverture des épaules, respiration profonde, marche courte. La chaleur douce, comme une douche tiède, peut soulager certaines tensions. Évitez les massages profonds non encadrés, les manipulations brusques et les exercices qui provoquent une douleur nette.

Consultez si la douleur est intense, persistante, descend dans la jambe avec faiblesse ou engourdissement, s’accompagne de fièvre, de contractions régulières, de saignements ou de brûlures urinaires. Une sage-femme, un médecin ou un kinésithérapeute formé à la grossesse peut proposer des exercices adaptés. Le dos mérite une attention précoce: plus vous ajustez les gestes du quotidien, plus vous réduisez le risque que la douleur s’installe.$article$,
    '/article-images/article_activite_dos.jfif',
    4,
    '/videos/Entra%C3%AEnement%20pr%C3%A9natal%20%20_%20bouger%20durant%20la%20grossesse.mp4',
    'Entraînement prénatal doux'
),
(
    'Stress, émotions et respiration',
    'Mental',
    $article$La grossesse peut être une période de joie, mais aussi d’inquiétudes. Les rendez-vous médicaux, les changements du corps, la fatigue, les questions financières, familiales ou professionnelles peuvent augmenter le stress. Ressentir des émotions variables ne signifie pas que vous êtes faible. Cela signifie souvent que vous traversez une étape importante, avec beaucoup d’informations à intégrer.

La respiration est un outil simple pour revenir au présent. Vous pouvez essayer quelques cycles: inspirer doucement par le nez, laisser le ventre et les côtes s’ouvrir sans forcer, puis expirer plus lentement que l’inspiration. Répétez pendant deux ou trois minutes. L’objectif n’est pas de supprimer toutes les pensées, mais de donner au corps un signal de calme. Certaines femmes aiment ajouter une main sur le ventre, une musique douce ou une phrase rassurante.

Écrire peut aussi aider. Notez ce qui vous inquiète, puis séparez ce qui dépend de vous de ce qui devra être discuté avec un professionnel. Préparer une liste de questions pour la consultation évite de garder tout dans la tête. Parler au co-parent, à une amie, à une sage-femme ou à un psychologue peut alléger la charge émotionnelle.

Demandez de l’aide si l’anxiété devient envahissante, si vous pleurez souvent, si vous dormez très mal, si vous n’arrivez plus à profiter de moments simples, ou si des pensées inquiétantes persistent. La santé mentale fait partie du suivi de grossesse. Il n’y a pas besoin d’attendre d’aller très mal pour en parler. Un accompagnement précoce peut changer beaucoup de choses.$article$,
    '/article-images/article_mental_respiration.jpg',
    5,
    '/videos/Breathing%20exercise%20for%20stress%20and%20anxiety.mp4',
    'Respiration douce contre le stress'
),
(
    'Préparer son cercle de soutien',
    'Mental',
    $article$Préparer l’arrivée d’un bébé ne concerne pas seulement les achats ou les rendez-vous. Le soutien humain est essentiel. Pendant la grossesse et après la naissance, vous aurez peut-être besoin d’écoute, de repos, d’aide pratique, de repas simples, de transport, ou simplement de quelqu’un qui respecte vos choix. Construire ce cercle à l’avance permet de réduire la pression quand la fatigue augmente.

Commencez par identifier les personnes fiables: co-parent, famille, amie, voisine, collègue, sage-femme, médecin, association locale. Tout le monde n’a pas le même rôle. Une personne peut être excellente pour écouter, une autre pour aider avec les courses, une autre pour garder un aîné. Soyez concrète dans vos demandes: "peux-tu m’accompagner à ce rendez-vous", "peux-tu apporter un repas", "peux-tu m’appeler une fois par semaine". Les demandes précises sont plus faciles à accepter.

Il est aussi utile de définir vos limites. Certaines visites peuvent être fatigantes, certains commentaires peuvent blesser, et vous avez le droit de protéger votre calme. Préparez quelques phrases simples: "nous vous préviendrons quand nous serons prêts", "j’ai besoin de repos", "je préfère suivre l’avis de ma sage-femme". Le soutien doit vous aider, pas vous envahir.

Si vous vous sentez isolée, parlez-en pendant le suivi. Les professionnels peuvent orienter vers des groupes de préparation, des réseaux de périnatalité, des consultations psychologiques ou des aides sociales selon la situation. Demander du soutien n’enlève rien à votre capacité de devenir mère. Au contraire, cela crée un environnement plus sécurisant pour vous et votre bébé.$article$,
    '/article-images/article_mental_soutien.jpg',
    4,
    '/videos/Comment%20aider%20quelqu%E2%80%99un%20%20pendant%20une%20crise%20de%20panique.mp4',
    'Aider pendant une crise de panique'
),
(
    'Bien préparer une consultation prénatale',
    'Rendez-vous',
    $article$Une consultation prénatale passe souvent vite. Entre les mesures, les questions médicales et les informations données, il est facile d’oublier un sujet important. La préparation permet de repartir avec des réponses plus claires. Avant le rendez-vous, notez vos symptômes récents, même ceux qui semblent petits: douleurs, fatigue, sommeil, digestion, contractions, mouvements du bébé, humeur, pertes, maux de tête ou gonflements.

Apportez vos documents: résultats d’analyses, échographies, ordonnances, carte de groupe sanguin si vous en avez une, liste des traitements ou compléments, et carnet de suivi. Si vous utilisez l’application pour noter le poids, les rendez-vous ou les questions, ouvrez-la pendant la consultation. Les données régulières aident parfois à décrire une évolution plus précisément qu’un souvenir vague.

Préparez trois questions prioritaires. Par exemple: "ce symptôme est-il normal", "quels signes doivent me faire appeler", "puis-je continuer cette activité", "comment adapter mon alimentation", "quand aura lieu le prochain examen". Si vous avez peur de poser une question, rappelez-vous qu’aucune inquiétude n’est ridicule. Les professionnels préfèrent souvent expliquer plutôt que vous laisser chercher seule.

Après le rendez-vous, prenez deux minutes pour noter les consignes reçues: examens à faire, date du prochain contrôle, médicaments autorisés ou non, signes d’alerte, recommandations personnalisées. Si une information n’est pas claire, contactez le cabinet ou la sage-femme plutôt que d’interpréter seule. Une bonne consultation n’est pas seulement un contrôle médical; c’est un moment pour comprendre votre grossesse et participer aux décisions qui vous concernent.$article$,
    '/article-images/article_rdv_consultation.jfif',
    5,
    '/videos/K0013980-Pregnant_woman_ultrasound.mp4',
    'Échographie pendant la grossesse'
),
(
    'Échographies et questions utiles',
    'Rendez-vous',
    $article$Les échographies sont des moments attendus, parfois émouvants, parfois stressants. Elles permettent d’observer certains éléments du développement du bébé, sa croissance, sa position, le placenta et d’autres repères selon le terme. Elles ne montrent pas tout, et leur objectif n’est pas seulement d’obtenir une image souvenir. Les comprendre comme un examen médical aide à mieux vivre le rendez-vous.

Avant l’échographie, vérifiez les consignes: heure, lieu, documents à apporter, vessie pleine ou non selon l’examen, présence possible du co-parent. Notez vos questions. Vous pouvez demander ce qui est observé, si la croissance correspond au terme, où se situe le placenta, quand aura lieu le prochain contrôle, et quels signes doivent vous faire consulter avant le rendez-vous suivant. Si un terme médical est utilisé, demandez une explication simple.

Pendant l’examen, il peut y avoir des moments silencieux. Cela ne signifie pas automatiquement qu’il y a un problème; le professionnel peut être concentré sur les mesures. Si l’attente vous inquiète, dites-le calmement à la fin et demandez ce qui a été vérifié. Après l’examen, gardez le compte rendu avec vos documents et évitez de comparer chaque mesure avec des tableaux trouvés en ligne. Les mesures doivent être interprétées dans l’ensemble du dossier.

Si une anomalie ou un doute est évoqué, il est normal d’être bouleversée. Demandez quelles sont les prochaines étapes, à quel délai, et qui contacter en cas de question. Ne restez pas seule avec des mots incompris. Votre médecin ou sage-femme peut reformuler les résultats et vous orienter si un avis spécialisé est nécessaire.$article$,
    '/article-images/article_rdv_echographie.jfif',
    5,
    '/videos/K0013980-Pregnant_woman_ultrasound.mp4',
    'Échographie pendant la grossesse'
),
(
    'Préparer la valise de maternité sereinement',
    'Conseils',
    $article$Préparer la valise de maternité permet de se sentir plus prête, même si l’arrivée du bébé garde toujours une part d’imprévu. L’idéal est de commencer quelques semaines avant le terme, puis d’ajuster selon les consignes de votre maternité. Chaque établissement a ses habitudes, donc la liste donnée pendant les cours ou les rendez-vous reste la référence.

Pour vous, pensez au confort: vêtements faciles à enfiler, sous-vêtements adaptés, protections, nécessaire de toilette, serviette, chargeur, bouteille d’eau, documents administratifs et médicaux. Une tenue ample pour la sortie peut être plus agréable qu’un vêtement serré. Si vous souhaitez allaiter, prévoyez des vêtements pratiques, mais gardez en tête que l’accompagnement sur place compte plus que le matériel. Pour le bébé, préparez quelques bodies, pyjamas, bonnet, chaussettes, couches si demandées, couverture ou gigoteuse selon les consignes.

Il peut être utile de séparer les affaires en pochettes: documents, salle de naissance, séjour, bébé, retour à la maison. Le co-parent ou la personne accompagnante trouvera plus facilement ce qu’il faut si vous êtes fatiguée. Gardez les papiers importants dans une pochette accessible: carte d’identité, carte vitale ou assurance, groupe sanguin, résultats récents, dossier de grossesse.

Ne cherchez pas la valise parfaite. Beaucoup de choses peuvent être apportées plus tard si besoin. L’essentiel est d’avoir les documents, de quoi vous sentir propre et confortable, et les premières affaires du bébé. Si vous avez une césarienne programmée, une grossesse à risque ou des besoins particuliers, demandez une liste adaptée. Préparer la valise est aussi une manière douce de se projeter vers la rencontre.$article$,
    'local://article_conseils_valise',
    5,
    '/videos/Pr%C3%A9parer%20la%20valise%20de%20b%C3%A9b%C3%A9%20pour%20la%20maternit%C3%A9.mp4',
    'Préparer la valise de maternité'
),
(
    'Retour à la maison les premiers jours',
    'Conseils',
    $article$Le retour à la maison peut être très attendu, mais il peut aussi être déroutant. Le rythme change, les nuits sont courtes, le corps récupère, et les émotions peuvent être intenses. Il est normal de ne pas tout maîtriser immédiatement. Les premiers jours servent surtout à créer des repères simples: se nourrir, se reposer, observer le bébé, comprendre ses besoins et accepter l’aide disponible.

Préparez l’environnement avant la naissance si possible: quelques repas faciles, une zone de change, des vêtements propres, les numéros utiles, et un espace confortable pour nourrir le bébé. Limitez les visites si elles vous fatiguent. Vous pouvez annoncer clairement que vous avez besoin de calme et que vous proposerez un moment plus tard. Le repos de la mère est une priorité, pas un luxe.

Surveillez aussi votre récupération. Des douleurs, saignements, montée de lait, cicatrice, fatigue ou émotions fortes peuvent être présents, mais certains signes doivent conduire à demander rapidement un avis: fièvre, saignements très abondants, douleur intense, mauvaise odeur des pertes, douleur au mollet, essoufflement, tristesse profonde, pensées inquiétantes ou sentiment de ne plus pouvoir faire face. Votre sage-femme, médecin ou maternité peut vous guider.

Concernant le bébé, posez toutes vos questions sur l’alimentation, le sommeil, les pleurs, le poids ou le cordon. Aucun parent ne sait tout au départ. Gardez en tête que demander de l’aide tôt évite souvent que les difficultés s’installent. Le retour à la maison est une transition, pas un examen. Avancez jour après jour, avec des attentes réalistes et un soutien adapté.$article$,
    '/article-images/article_conseils_retour.jfif',
    5,
    NULL,
    NULL
);

UPDATE articles AS article
SET
    category = seed.category,
    content = seed.content,
    image_url = seed.image_url,
    reading_time_minutes = seed.reading_time_minutes,
    video_url = seed.video_url,
    video_title = seed.video_title
FROM mama_article_seed AS seed
WHERE LOWER(article.title) = LOWER(seed.title);

INSERT INTO articles (title, category, content, image_url, reading_time_minutes, video_url, video_title, created_at)
SELECT
    seed.title,
    seed.category,
    seed.content,
    seed.image_url,
    seed.reading_time_minutes,
    seed.video_url,
    seed.video_title,
    NOW()
FROM mama_article_seed AS seed
WHERE NOT EXISTS (
    SELECT 1
    FROM articles AS article
    WHERE LOWER(article.title) = LOWER(seed.title)
);

-- Remove the previous short demo seed rows only when they are still clearly placeholders.
DELETE FROM articles
WHERE image_url IS NULL
  AND LENGTH(content) < 700
  AND title IN (
      'Bien manger au premier trimestre',
      'Hydratation pendant la grossesse',
      'Comprendre les signes a surveiller',
      'Sommeil et fatigue',
      'Le developpement de bebe semaine apres semaine',
      'Sentir les premiers mouvements',
      'Activite physique douce',
      'Etirements pour le dos',
      'Gerer le stress et les emotions',
      'Preparer les rendez-vous medicaux',
      'Organiser la valise de maternité',
      'Petits repas contre les nausees'
  );

DROP TABLE IF EXISTS mama_article_seed;
