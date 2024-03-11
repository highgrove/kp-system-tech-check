# Tech-check for KP system

This is a solution for the tech check given to me (Niklas Höglund) from the company KP System. 
For future reference the task description can be found in the docs catalog [`Kodprov KP System_2024.pdf`)](./docs/Kodprov KP System_2024.pdf)

## Choises made
I have chosen to use the recomended `re-frame` library for the frontend part of the application, and `tailwindcss` for fun. I feel i made a bad choise in using `google-map-react`, should have investigated my options more. I took the chanse in doing a tiny bit of backend with `pedestal`.

## Getting Started

Download links:

SSH clone URL: ssh://git@git.jetbrains.space/prozest/kp-system-tech-check/kp-system-tech-check.git

HTTPS clone URL: https://git.jetbrains.space/prozest/kp-system-tech-check/kp-system-tech-check.git

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Run the application

Have a goolgle maps api key ready.
1. `npm install`
2. `npm run tailwind:build`
3. `npm run watch -- --config-merge '{:closure-defines {kp.tech-check.config/GOOGLE_MAPS_API_KEY $$YOUR_GOOGLE_MAPS_API_KEY$$}}`
4. `clj -M:dev/backend`

Goto 'http://localhost:8280/`

## Resources

[tailwindcss](https://tailwindcss.com)
[clojure](https://clojure.org/)
[google-map-react](https://github.com/google-map-react/google-map-react)
[pedestal.io](http://pedestal.io/pedestal/0.6/index.html)
