INSERT INTO provider_routing_policy (
    provider,
    weight,
    enabled,
    version,
    created_at,
    updated_at
) VALUES
    ('KG', 10, TRUE, 1, NOW(6), NOW(6)),
    ('NICE', 90, TRUE, 1, NOW(6), NOW(6));
