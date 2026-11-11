# Changelog

## [0.0.1](https://github.com/Hamza-Zengin/SekCLib/compare/v1.1.0...v0.0.1) (2026-11-11)


### ⚠ BREAKING CHANGES

* 1.19 support dropped

### Features ✨

* add @Sync annotations for capabilities ([8df3532](https://github.com/Hamza-Zengin/SekCLib/commit/8df353241b81a6fef010fd821fd693427e1b2863))
* add byte sync tracker ([00ea96f](https://github.com/Hamza-Zengin/SekCLib/commit/00ea96f600cb5d50cc7d13a06c75a8d97617c300))
* add the ability to sync null values ([f0580f8](https://github.com/Hamza-Zengin/SekCLib/commit/f0580f8400c260d0f2d0a8bf43ffc8a6e939cddd))
* Add utils originally from the Naruto Mod ([a5e1a3c](https://github.com/Hamza-Zengin/SekCLib/commit/a5e1a3c51ded49335e1c47bb6a01602642053453))
* add vec3 sync tracker ([f62ae6f](https://github.com/Hamza-Zengin/SekCLib/commit/f62ae6fb0cc6266c0d570523eedf6fd36d142191))
* build's should now also be available on my maven ([c57c8d2](https://github.com/Hamza-Zengin/SekCLib/commit/c57c8d2a164d117e88cdc6ce6f45c701133f57cf))
* optimize capability tick handler, update CI workflows, and expand documentation ([2e859c5](https://github.com/Hamza-Zengin/SekCLib/commit/2e859c52a92d4f974c144eb41d0da60f0f3e564c))
* release 1.1.0 - harden packet sync, add unit tests, color util fixes, and Naruto-Mod compat ([32bd885](https://github.com/Hamza-Zengin/SekCLib/commit/32bd88516e36608e207b67ac077df2a780a5ff3e))
* **sync:** add CompoundTag and Set sync trackers, unify PlayerUtil, and support generic types ([efac9d6](https://github.com/Hamza-Zengin/SekCLib/commit/efac9d697aaabae2f11f1998fc063795d2d6446b))
* update to 1.18.2 ([385d57c](https://github.com/Hamza-Zengin/SekCLib/commit/385d57c307e388fc2c1319a266e26c5158edfbc5))
* Update to 1.19 ([a5c8c64](https://github.com/Hamza-Zengin/SekCLib/commit/a5c8c64a66f629d96b4b151e2f1ccf7365c398eb))
* updated to 1.20+ ([02c3381](https://github.com/Hamza-Zengin/SekCLib/commit/02c3381c09d736bd2a3b56d4f95f8a263c8bb647))


### Bug Fixes 🐛

* add support for 1.19.3 ([f474b6b](https://github.com/Hamza-Zengin/SekCLib/commit/f474b6bce46c66370448ae687fb1d7ddbe794da3))
* alter range of supported mc versions to be more precise ([08bfe4e](https://github.com/Hamza-Zengin/SekCLib/commit/08bfe4e531d78595b15b9f79de316d2997be9af0))
* **build:** guard optional release plugins and ensure offline jar compilation ([03fa81f](https://github.com/Hamza-Zengin/SekCLib/commit/03fa81f11da131d5b032487c12f788ac9af61728))
* **build:** support isRelease property toggle for local and CI builds ([77642b9](https://github.com/Hamza-Zengin/SekCLib/commit/77642b960a4046a706c745046ebc442a32b37c44))
* bump to support 1.19.1 + 1.19.2, no code changes ([e112377](https://github.com/Hamza-Zengin/SekCLib/commit/e112377de46dd154071e3b7910cba2b4713dd75f))
* **ci:** automated discord uploading ([b528b4a](https://github.com/Hamza-Zengin/SekCLib/commit/b528b4a260765f618e7ab184b5e5fc0740984d13))
* **ci:** grant write permissions and add GITHUB_TOKEN fallback for release and docs deployment ([bde2416](https://github.com/Hamza-Zengin/SekCLib/commit/bde2416ad9261babef0a9bfba067ecd3701cf89c))
* class name updateds to avoid crashing ([ab0dfd2](https://github.com/Hamza-Zengin/SekCLib/commit/ab0dfd24663b3fcad5138aa986a30c0b6ef9e6d3))
* fix mixin requirement ([acc0001](https://github.com/Hamza-Zengin/SekCLib/commit/acc0001dc6a3120cbc0130f9d1efa54e9fa700e3))
* remove mixin from manifest ([070ff16](https://github.com/Hamza-Zengin/SekCLib/commit/070ff16aa8ddfad2ef4a26c6bbc460ac928e9e54))
* **sync:** extract live capability data on full sync and add login/respawn events ([97a4dfe](https://github.com/Hamza-Zengin/SekCLib/commit/97a4dfec77b3f7a49cb60db90b1f28b56616ed6e))
* widen support to support 1.20+ as it's not likely it will break across sub-versions ([a0695cd](https://github.com/Hamza-Zengin/SekCLib/commit/a0695cdb6077c9aa16c4f88c491f1c2c71953c8b))
* wrong basename function called causing strange jar name ([15bf4b3](https://github.com/Hamza-Zengin/SekCLib/commit/15bf4b3fd83a3f4e2cae3803cabf89b9c3e01170))


### Performance Improvements ⚡️

* **sync:** optimize capability broadcaster, add epsilon delta tracking, and packet targeting ([38f2ac3](https://github.com/Hamza-Zengin/SekCLib/commit/38f2ac3c3e3e2e5f10edd754be2c018057595ad0))

## [1.0.2](https://github.com/sekwah41/SekCLib/compare/v1.0.1...v1.0.2) (2023-06-22)


### Bug Fixes 🐛

* wrong basename function called causing strange jar name ([15bf4b3](https://github.com/sekwah41/SekCLib/commit/15bf4b3fd83a3f4e2cae3803cabf89b9c3e01170))

## [1.0.1](https://github.com/sekwah41/SekCLib/compare/v1.0.0...v1.0.1) (2023-06-22)


### Bug Fixes 🐛

* **ci:** automated discord uploading ([b528b4a](https://github.com/sekwah41/SekCLib/commit/b528b4a260765f618e7ab184b5e5fc0740984d13))
* widen support to support 1.20+ as it's not likely it will break across sub-versions ([a0695cd](https://github.com/sekwah41/SekCLib/commit/a0695cdb6077c9aa16c4f88c491f1c2c71953c8b))

## [1.0.0](https://github.com/sekwah41/SekCLib/compare/v0.4.3...v1.0.0) (2023-06-21)


### ⚠ BREAKING CHANGES

* 1.19 support dropped

### Features ✨

* updated to 1.20+ ([02c3381](https://github.com/sekwah41/SekCLib/commit/02c3381c09d736bd2a3b56d4f95f8a263c8bb647))

## [0.4.3](https://github.com/sekwah41/SekCLib/compare/v0.4.2...v0.4.3) (2023-01-15)


### Bug Fixes 🐛

* add support for 1.19.3 ([f474b6b](https://github.com/sekwah41/SekCLib/commit/f474b6bce46c66370448ae687fb1d7ddbe794da3))

## [0.4.2](https://github.com/sekwah41/SekCLib/compare/v0.4.1...v0.4.2) (2022-08-13)


### Bug Fixes 🐛

* bump to support 1.19.1 + 1.19.2, no code changes ([e112377](https://github.com/sekwah41/SekCLib/commit/e112377de46dd154071e3b7910cba2b4713dd75f))

## [0.4.1](https://github.com/sekwah41/SekCLib/compare/v0.4.0...v0.4.1) (2022-07-12)


### Bug Fixes 🐛

* class name updateds to avoid crashing ([ab0dfd2](https://github.com/sekwah41/SekCLib/commit/ab0dfd24663b3fcad5138aa986a30c0b6ef9e6d3))

## [0.4.0](https://github.com/sekwah41/SekCLib/compare/v0.3.1...v0.4.0) (2022-06-12)


### Features ✨

* Update to 1.19 ([a5c8c64](https://github.com/sekwah41/SekCLib/commit/a5c8c64a66f629d96b4b151e2f1ccf7365c398eb))

### [0.3.1](https://github.com/sekwah41/SekCLib/compare/v0.3.0...v0.3.1) (2022-04-23)


### Bug Fixes 🐛

* alter range of supported mc versions to be more precise ([08bfe4e](https://github.com/sekwah41/SekCLib/commit/08bfe4e531d78595b15b9f79de316d2997be9af0))

## [0.3.0](https://github.com/sekwah41/SekCLib/compare/v0.2.0...v0.3.0) (2022-03-24)


### Features ✨

* update to 1.18.2 ([385d57c](https://github.com/sekwah41/SekCLib/commit/385d57c307e388fc2c1319a266e26c5158edfbc5))

## [0.2.0](https://github.com/sekwah41/SekCLib/compare/v0.1.0...v0.2.0) (2022-03-06)


### Features ✨

* add byte sync tracker ([00ea96f](https://github.com/sekwah41/SekCLib/commit/00ea96f600cb5d50cc7d13a06c75a8d97617c300))
* add the ability to sync null values ([f0580f8](https://github.com/sekwah41/SekCLib/commit/f0580f8400c260d0f2d0a8bf43ffc8a6e939cddd))

## [0.1.0](https://github.com/sekwah41/SekCLib/compare/v0.0.2...v0.1.0) (2022-03-06)


### Features ✨

* add vec3 sync tracker ([f62ae6f](https://github.com/sekwah41/SekCLib/commit/f62ae6fb0cc6266c0d570523eedf6fd36d142191))

### [0.0.2](https://github.com/sekwah41/SekCLib/compare/v0.0.1...v0.0.2) (2022-03-06)


### Bug Fixes 🐛

* fix mixin requirement ([acc0001](https://github.com/sekwah41/SekCLib/commit/acc0001dc6a3120cbc0130f9d1efa54e9fa700e3))
* remove mixin from manifest ([070ff16](https://github.com/sekwah41/SekCLib/commit/070ff16aa8ddfad2ef4a26c6bbc460ac928e9e54))

### 0.0.1 (2022-03-06)


### Features ✨

* add @Sync annotations for capabilities ([8df3532](https://github.com/sekwah41/SekCLib/commit/8df353241b81a6fef010fd821fd693427e1b2863))
* Add utils originally from the Naruto Mod ([a5e1a3c](https://github.com/sekwah41/SekCLib/commit/a5e1a3c51ded49335e1c47bb6a01602642053453))
* build's should now also be available on my maven ([c57c8d2](https://github.com/sekwah41/SekCLib/commit/c57c8d2a164d117e88cdc6ce6f45c701133f57cf))
