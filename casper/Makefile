prepare:
	rustup target add wasm32-unknown-unknown

build-did-registry:
	cd did_registry && cargo build --release --target wasm32-unknown-unknown
	wasm-strip target/wasm32-unknown-unknown/release/did_registry.wasm 2>/dev/null | true

test: build-did-registry
	mkdir -p tests/wasm
	cp target/wasm32-unknown-unknown/release/did_registry.wasm tests/wasm
	cd tests && cargo test

clippy:
	cd did_common && cargo clippy --all-targets -- -D warnings
	cd did_registry && cargo clippy --all-targets -- -D warnings
	cd tests && cargo clippy --all-targets -- -D warnings

check-lint: clippy
	cd did_registry && cargo fmt -- --check
	cd tests && cargo fmt -- --check

lint: clippy
	cd did_registry && cargo fmt
	cd tests && cargo fmt

clean:
	cd did_registry && cargo clean
	cd tests && cargo clean
	rm -rf tests/wasm
