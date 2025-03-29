DROP TABLE IF EXISTS urls;

CREATE TABLE urls (
    id SERIAL PRIMARY KEY,
    original_url TEXT NOT NULL,
    shorten_url TEXT NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT original_url_not_empty CHECK (original_url <> ''),
    CONSTRAINT shorten_url_not_empty CHECK (shorten_url <> '')
);

-- Create a trigger to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_url_updated_at
BEFORE UPDATE ON urls
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Create an index on the shorten_url column for faster lookups
CREATE INDEX idx_shorten_url ON urls (shorten_url);